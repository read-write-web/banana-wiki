/**
  * Good documentation for fs2 "My fs2 (was scalaz-stream) User Notes" at
  *  https://aappddeevv.gitbooks.io/test_private_book/details
  *
  * you can run this from ammonite shell with:
  
     import $exec.http4s
     //implicit val F = fs2.Async[fs2.Task]
     val web = new Web()
     val gf2 = web.goodFriend(URI("http://bblfish.net/people/henry/card#me"))
     val friendTask = gf2.runLog
     val friendFut = friendTask.unsafeRunAsyncFuture
*/
     

import coursier.core.Authentication, coursier.MavenRepository
import coursier.core.Authentication, coursier.MavenRepository
interp.repositories() ++= Seq(MavenRepository(
                "http://bblfish.net/work/repo/snapshots/"
                ))

@

import $ivy.`org.w3::banana-sesame:0.8.4-SNAPSHOT`
import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.sesame.Sesame
import Sesame._
import Sesame.ops._

import $ivy.`org.http4s::http4s-dsl:0.17.0-M1`
import $ivy.`org.http4s::http4s-blaze-client:0.17.0-M1`, org.http4s.client.blaze._
import java.util.concurrent.ThreadPoolExecutor

//testing concurrency
implicit val strat = fs2.Strategy.fromFixedDaemonPool(8)

val Rdf = Sesame

object RdfMediaTypes {
   import org.http4s._
   //not all the rdf formats are listed here. This is just the beginning. Check the extensions.
   //also check for older media type usages
   val `text/turtle` = new MediaType("text","turtle", fileExtensions=Seq("ttl"))
   val `application/rdf+xml` = new MediaType("application","rdf+xml",fileExtensions=Seq("rdf"))
   val `application/ntriples` = new MediaType("application","ntriples",fileExtensions=Seq("nt"))
   val `application/ld+json` = new MediaType("application","ld+json",fileExtensions=Seq("jsonld"))
}

object Decoders {
   import RdfMediaTypes._
   import scala.util.{Try,Success,Failure}
   import org.http4s._
   import org.w3.banana.io.RDFReader 
   import cats.data._

   private def decoderForRdfReader(mt: MediaRange, mts: MediaRange*)(reader: RDFReader[Rdf,Try,_], errmsg: String ) = 
    EntityDecoder.decodeBy[Rdf#Graph](mt,mts: _*){ msg =>
       println(s"decoding in ${Thread.currentThread.getName} fetched ")
       EitherT(
          msg.as[String].flatMap[Either[DecodeFailure,Rdf#Graph]]{ s =>
             fs2.Task.delay(
                reader.read(new java.io.StringReader(s),"") match {
                  case Success(g) => Right(g)
                  case Failure(err) => Left(MalformedMessageBodyFailure(errmsg,Some(err)))
                }
             )
          }
      )
   }

   implicit val turtle = decoderForRdfReader(`text/turtle`)(turtleReader,"Rdf Turtle Reader failed") 

   implicit val rdfxml = decoderForRdfReader(`application/rdf+xml`)(rdfXMLReader,"Rdf Rdf/XML Reader failed")  

   implicit val ntriples = decoderForRdfReader( `application/ntriples`)(ntriplesReader,"NTriples  Reader failed")  

   implicit val jsonld = decoderForRdfReader(`application/ld+json`)(jsonldReader,"Json-LD Reader failed")  

   implicit val allrdf = turtle orElse rdfxml orElse ntriples orElse jsonld
}

object Web {
   import org.http4s
   implicit class UriW(val uri: http4s.Uri)  extends AnyVal {
         def fragmentLess: http4s.Uri = 
            if (uri.fragment.isEmpty) uri else uri.copy(fragment=None)
    }
}


class Web(implicit val strat: fs2.Strategy) {
   import scala.util.control.NoStackTrace
   import scala.util.{Either,Right,Left}
   import org.http4s 
   import fs2.util.Attempt
   import Web._
   
   val threadPooleExec = new ThreadPoolExecutor(2,3,20,java.util.concurrent.TimeUnit.SECONDS,new java.util.concurrent.LinkedBlockingQueue[Runnable])
   val dbc =org.http4s.client.blaze.BlazeClientConfig.defaultConfig
   val minBlazeConfig = dbc.copy(customExecutor=Some(threadPooleExec))
   val httpClient = org.http4s.client.middleware.FollowRedirect(4)(PooledHttp1Client(50,config=minBlazeConfig))


    case class HTTPException(on: http4s.Uri, exception: java.lang.Throwable) extends RuntimeException with NoStackTrace with Product with Serializable

   def toPointedTask(point: Rdf#Node, task: fs2.Task[Attempt[Rdf#Graph]]) = task.map(att=>att.map(g=>PointedGraph[Rdf](point, g)))

   def getPointed(uri: Rdf#URI): fs2.Task[Attempt[PointedGraph[Rdf]]] = {
       import Decoders.allrdf
       import org.http4s.{Request,Headers,Header}
       val u = http4s.Uri.fromString(uri.toString).right.get
       val req = new Request(
                     uri=u.fragmentLess, 
                     headers=Headers(
                        Header("Accept","application/rdf+xml,text/turtle,application/ld+json,text/n3;q=0.2"))
                )
       val graphTsk = fs2.Task({
               println(s"${Thread.currentThread.getName} to fetch $uri")
               emptyGraph
       }).async.flatMap( _ =>
               httpClient.fetchAs[Rdf#Graph](req).handleWith({ case err => fs2.Task.fail(HTTPException(u,err)) })
       ).attempt  
       toPointedTask(uri,graphTsk).async
   }

  def jump(pg: PointedGraph[Rdf], rel: Rdf#URI): fs2.Stream[fs2.Task,Attempt[PointedGraph[Rdf]]] = {
     val pgTaskSeq = (pg/rel).toSeq.collect {
           case PointedGraph(u,_) if u.isURI => 
              getPointed(u.asInstanceOf[Rdf#URI]) //<- the case match should deal with type
     } 
     fs2.Stream.emits(pgTaskSeq).flatMap(fs2.Stream.eval)
  }
   
   val foaf = FOAFPrefix[Rdf]
   def goodFriend(uri: Rdf#URI) = {
      fs2.Stream.eval(getPointed(uri)).flatMap(_ match {
           case Right(pg) => jump(pg,foaf.knows) 
           case l@Left(e) => fs2.Stream(l) 
      })
   }
   
}

