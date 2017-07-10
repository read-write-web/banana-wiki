import coursier.core.Authentication, coursier.MavenRepository

interp.repositories() ++= Seq(MavenRepository(
  "http://bblfish.net/work/repo/snapshots/"
  ))

@

import scala.concurrent.ExecutionContext

import $ivy.`org.w3::banana-sesame:0.8.4-SNAPSHOT`
import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.sesame.Sesame
import Sesame._
import Sesame.ops._

import $ivy.`com.typesafe.akka::akka-http:10.0.9` 

import akka.actor.ActorSystem
import akka.actor.SupervisorStrategy
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{Uri=>AkkaUri,_}

import akka.stream.{ActorMaterializer, ActorMaterializerSettings,Supervision,_}
import akka.stream.scaladsl._
import akka.{ NotUsed, Done }

import scala.concurrent.Future
import scala.util.control.NoStackTrace 
import scala.util.{Try,Success,Failure}

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer(
                ActorMaterializerSettings(system).withSupervisionStrategy(Supervision.resumingDecider))
implicit val ec: ExecutionContext = system.dispatcher

//just to play with 
val me = AkkaUri("http://bblfish.net/people/henry/card#me")

object RdfMediaTypes {
   import akka.http.scaladsl.model
   import model.ContentType
   import model.MediaType.{text,applicationWithOpenCharset,applicationWithFixedCharset}
   import model.HttpCharsets._
   import org.w3.banana.io.RDFReader 
   import akka.http.scaladsl.unmarshalling.{Unmarshaller,PredefinedFromEntityUnmarshallers,FromEntityUnmarshaller}
   import scala.util.{Try,Success,Failure}

    case class NoUnmarshallerException(mime: ContentType, msg: String) extends java.lang.RuntimeException with NoStackTrace with Product with Serializable

   val `text/turtle` = text("turtle","ttl")
   val `application/rdf+xml` = applicationWithOpenCharset("rdf+xml","rdf")
   val `application/ntriples` = applicationWithFixedCharset("ntriples",`UTF-8`,"nt")
   val `application/ld+json` = applicationWithOpenCharset("ld+json","jsonld")

   
   def rdfUnmarshaller(requestUri: AkkaUri): FromEntityUnmarshaller[Rdf#Graph] = {
        import Unmarshaller._
        val rdfunmarshaller = PredefinedFromEntityUnmarshallers.stringUnmarshaller mapWithInput { (entity, string) ⇒ 
           val reader = entity.contentType.mediaType match { //<- this needs to be tuned!
              case `text/turtle` => turtleReader
              case `application/rdf+xml` => rdfXMLReader
              case `application/ntriples` => ntriplesReader
              case `application/ld+json` => jsonldReader
              //case _ => throw NoUnmarshallerException(entity.contentType,"Don't know how to transform such a mime type into an RDFGraph")  
           }
           reader.read(new java.io.StringReader(string),requestUri.toString) match {
            case Success(g) => g
            case Failure(err) => if (string.isEmpty) throw Unmarshaller.NoContentException else throw err
            //throwing errors seems to be the thing to do in Akka
         } 
       }
      rdfunmarshaller.forContentTypes(`text/turtle`,`application/rdf+xml`,`application/ntriples`,`application/ld+json`) 
  }

}

object Web {
   
    case class HTTPException(on: AkkaUri, msg: String) extends java.lang.RuntimeException with NoStackTrace with Product with Serializable

   implicit class UriW(val uri: AkkaUri)  extends AnyVal {
         def fragmentLess: AkkaUri = 
            if (uri.fragment.isEmpty) uri else uri.copy(fragment=None)
         
         def toRdfUri: Rdf#URI = URI(uri.toString)
    }

   def rdfRequest(uri: AkkaUri): HttpRequest = {
      import RdfMediaTypes._  
      import akka.http.scaladsl.model.headers.Accept
      HttpRequest(uri=uri.fragmentLess)
           .addHeader(Accept(`text/turtle`,`application/rdf+xml`,
                             `application/ntriples`,`application/ld+json`))
   }

   //interpreted HttpResponse
   case class HttpRes[C](origin: AkkaUri, status: StatusCode, headers: Seq[HttpHeader], content: C) {
      def map[D](f: C => D) = this.copy(content=f(content))
   }


   implicit class HttResPG(val h: HttpRes[PointedGraph[Rdf]]) extends AnyVal {
    def jump(rel: Sesame#URI)(implicit web: Web): Seq[Future[HttpRes[PointedGraph[Rdf]]]] =
          (h.content/rel).toSeq.map{ pg =>
              if (h.content.pointer.isURI) web.pointedGET(AkkaUri(pg.pointer.toString))
              else Future.successful(h.copy(content=pg))
       }
    } 

    def reduceFlowFutureTry[X](n: Int=1)(log: X=>String= {a:X=>""}): Flow[Future[X],Try[X],_] =
       Flow[Future[X]].mapAsyncUnordered(n){ fx =>
           println("smushing a future")
           fx.transform{t=>
             t match {
               case Success(x) => println(log(x))
               case Failure(e) => println("t was a failure:"+e)
             }
             Success(t)
           }
       }

    import scala.collection.immutable 
    def uriSource(uris: AkkaUri*): Source[AkkaUri,NotUsed] =  
         Source(immutable.Seq(uris:_*).to[collection.immutable.Iterable])


   def goodFriendGraph(me: AkkaUri) = {
       import scala.collection.immutable
       val mine = uriSource(me)
       val flowJumpId = mine.mapAsync(1)(uri => web.pointedGET(uri))
       val flowJumpFutKn = flowJumpId.mapConcat{ hrpg => hrpg.jump(foaf.knows).to[immutable.Iterable] }
       val flowJumpTryKn = flowJumpFutKn.via(reduceFlowFutureTry[Web.HttpRes[PointedGraph[Rdf]]](50)(res=>"smushing ${res.origin}"))
       val sinkFold2 = Sink.fold[List[Try[Web.HttpRes[PointedGraph[Rdf]]]],
                                 Try[Web.HttpRes[PointedGraph[Rdf]]]](List()){ case (l,t)=>
                                   println(s"in Sink.fold. Appending $t to $l")
                                   t::l
                                  }
       flowJumpTryKn.toMat(sinkFold2)(Keep.right)
   }
 
}

class Web(implicit ec: ExecutionContext) {
   import Web._


   def GET(uri: AkkaUri, maxRedirect: Int=4): Future[HttpResponse] = httpRequire(rdfRequest(uri),maxRedirect)
 
   //todo: add something to the response re number of redirects
   //see: https://github.com/akka/akka-http/issues/195
   def httpRequire(req: HttpRequest, maxRedirect: Int = 4)(implicit 
      system: ActorSystem, mat: Materializer): Future[HttpResponse] = {
      Http().singleRequest(req).flatMap { resp =>
         resp.status match {
           case StatusCodes.Found => resp.header[headers.Location].map { loc =>
             val locUri = loc.uri
             val newUri = req.uri.copy(scheme = locUri.scheme, authority = locUri.authority)
             val newReq = req.copy(uri = newUri)
             if (maxRedirect > 0) httpRequire(newReq, maxRedirect - 1) else Http().singleRequest(newReq)
           }.getOrElse(Future.failed(HTTPException(req.uri,s"location not found on 302 for ${req.uri}")))
           case _ => Future(resp)
         }
      }
   }
   
  

   def GETrdf(uri: AkkaUri): Future[HttpRes[Rdf#Graph]] = {
       import akka.http.scaladsl.unmarshalling.Unmarshal
       import scala.util.control.NonFatal
       
       GET(uri).flatMap{
         case HttpResponse(status,headers,entity,protocol) => {
            implicit  val reqUnmarhaller = RdfMediaTypes.rdfUnmarshaller(uri)
            try {
               Unmarshal(entity).to[Rdf#Graph].map(g=>HttpRes[Rdf#Graph](uri,status,headers,g))
            } catch {
              case NonFatal(e) => Future.failed(HTTPException(uri,s"cought error unmarshaling: "+e))
            }
         }
       }
    }

    def pointedGET(uri: AkkaUri): Future[HttpRes[PointedGraph[Rdf]]] = 
         GETrdf(uri).map(httpresg=>httpresg.map(g=>PointedGraph[Rdf](URI(uri.toString),g)))
    

}

//make life easier in the shell by setting up the environment
implicit val web = new Web()
val foaf = FOAFPrefix[Rdf]
