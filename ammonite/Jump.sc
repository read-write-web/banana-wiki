//import $exec.FutureWrapper

/**
This is an ammonite script file that goes with the wiki page
at ../Scripting-with-Ammonite.md
This is a good starting point to try out improvements to the
code in the wiki. But if you make changes please also keep
those in sync with the Scripting-with-Ammonite.md page
so that folks following the course there can switch to using
this when they want to avoid copy pasting.

Run this as follows, from inside a new session of `amm` (Ammonite)

@ import $exec.Jump

You can then do something like the following:

@ val cache = new Cache()
@ val bblFuture = cache.getPointed(URI("http://bblfish.net/people/henry/card#me"))

*/

import coursier.core.Authentication, coursier.MavenRepository

interp.repositories() ++= Seq(MavenRepository(
  "http://bblfish.net/work/repo/snapshots/"
  ))

@

import $ivy.`org.w3::banana-jena:0.8.5-SNAPSHOT`
// import $file.RDFaBananaParser, RDFaBananaParser.SesameRDFaReader
import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.jena.Jena
import Jena._
import Jena.ops._
import scalaj.http._

type Rdf = Jena

val foaf = FOAFPrefix[Rdf]

import scala.util.{Try,Success,Failure}

case class IOException(docURL: Rdf#URI, e: java.lang.Throwable ) extends java.lang.Exception
def fetch(docUrl: Rdf#URI): Try[HttpResponse[scala.util.Try[Rdf#Graph]]] = {
  assert (docUrl == docUrl.fragmentLess)
  Try( //we want to catch connection exceptions
   Http(docUrl.getString)
    .header("Accept", "application/rdf+xml,text/turtle,application/ld+json,text/html;q=0.3,text/n3;q=0.2")
    .option{_.setInstanceFollowRedirects(true)}
    .exec { case (code, headers, is) =>
      headers.get("Content-Type")
             .flatMap(_.headOption)
             .fold[Try[(io.RDFReader[Rdf, Try, _],String)]](
                   Failure(new java.lang.Exception("Missing Content Type"))
              ) { ct =>
        val ctype = ct.split(';')
        val parser = ctype(0).trim match {
          case "text/turtle" => Success(turtleReader)
          case "text/n3" => Success(turtleReader) //perhaps we get something!
          case "application/rdf+xml" => Success(rdfXMLReader)
          case "application/n-triples" => Success(ntriplesReader)
          case "application/ld+json" => Success(jsonldReader)
          // case "text/html" => Success(new SesameRDFaReader())
          case ct => Failure(new java.lang.Exception("Missing parser for "+ct))
        }
        parser.map{ p =>
             val attributes = ctype.toList.tail.flatMap(
             _.split(',').map(_.split('=').toList.map(_.trim))
          ).map(avl => (avl.head, avl.tail.headOption.getOrElse(""))).toMap
          val encoding = attributes.get("encoding").getOrElse("utf-8")
          (p, encoding)
        }
      } flatMap { case (parser,encoding) =>
        parser.read(new java.io.InputStreamReader(is, encoding), docUrl.getString)
      }
    }).recoverWith{case t => Failure(IOException(docUrl,t))}
}

import scala.concurrent.{Future,ExecutionContext}
import java.util.concurrent.ThreadPoolExecutor
val threadPooleExec = new ThreadPoolExecutor(2,50,20,java.util.concurrent.TimeUnit.SECONDS,new java.util.concurrent.LinkedBlockingQueue[Runnable])
implicit lazy val webcontext = ExecutionContext.fromExecutor(threadPooleExec,err=>System.out.println("web error: "+err))

case class HttpException(docURL: Rdf#URI, code: Int, headers: Map[String, IndexedSeq[String]]) extends java.lang.Exception
case class ParseException(docURL: Rdf#URI, parseException: java.lang.Throwable) extends java.lang.Exception

class Cache(implicit val ex: ExecutionContext) {
   import scala.collection.mutable.Map
   val db = Map[Rdf#URI, Future[HttpResponse[Try[Rdf#Graph]]]]()
   def get(uri: Rdf#URI): Future[HttpResponse[Try[Rdf#Graph]]] = {
     val doc = uri.fragmentLess
     db.get(doc) match {
       case Some(f) => f
       case None => {
         val res = Future.fromTry( fetch(doc) )
         db.put( doc, res )
         res
       }
     }
  }

  def getPointed(uri: Rdf#URI): Future[PointedGraphWeb] =
     get(uri).flatMap{ response =>
         if (! response.is2xx ) Future.failed(HttpException(uri, response.code, response.headers))
         else {
           val moreTryInfo = response.body.recoverWith{ case t => Failure(ParseException(uri,t)) }
           Future.fromTry(moreTryInfo).map{g=>
               PointedGraphWeb(uri.fragmentLess,PointedGraph[Rdf](uri,g),response.headers.toMap)
           }
        }
     }

  case class PointedGraphWeb(webDocURL: Rdf#URI,
                           pointedGraph: PointedGraph[Rdf],
                           headers: scala.collection.immutable.Map[String,IndexedSeq[String]]) {
        def jump(rel: Rdf#URI): Seq[Future[PointedGraphWeb]] =
          (pointedGraph/rel).toSeq.map{ pg =>
              if (pg.pointer.isURI) Cache.this.getPointed(pg.pointer.asInstanceOf[Rdf#URI])
              else Future.successful(this.copy(pointedGraph=PointedGraph[Rdf](pg.pointer, pointedGraph.graph)))
       }
       def /(rel: Rdf#URI): Seq[PointedGraphWeb] = (pointedGraph/rel).toSeq.map(pg=>PointedGraphWeb(webDocURL,pg,headers))
       def point(uri: Rdf#URI) = this.copy(pointedGraph=PointedGraph[Rdf](uri,pointedGraph.graph))
    }
 }

def sequenceNoFail[A](seq :Seq[Future[A]]): Future[Seq[Try[A]]] =
     Future.sequence{ seq.map(_.transform(Success(_)))}

def conscientiousFriend(webID: Rdf#URI)(implicit cache: Cache): Future[Seq[(Rdf#URI, Option[PointedGraph[Rdf]])]] = {
    for{
      friendsFuture: Seq[Future[cache.PointedGraphWeb]] <- cache.getPointed(webID).map(_.jump(foaf.knows))
      triedFriends: Seq[Try[cache.PointedGraphWeb]] <- Future.sequence(
        friendsFuture.map (_.transform(tryPgw => Success(tryPgw)))
      )
    } yield {
      triedFriends.collect {
        case Success(cache.PointedGraphWeb(doc,pg,_)) =>
        val evidence = (pg/foaf.knows).filter((friend: PointedGraph[Rdf]) => webID == friend.pointer)
        (doc, evidence.headOption)
      }
    }
}
