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
  "http://bblfish.net/work/repo/releases/"
  ))

@

import $ivy.`org.w3::banana-sesame:0.8.4`
import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.sesame.Sesame
import Sesame._
import Sesame.ops._
import scalaj.http._

val foaf = FOAFPrefix[Sesame] 

import scala.util.{Try,Success,Failure}

case class IOException(docURL: Sesame#URI, e: java.lang.Throwable ) extends java.lang.Exception
def fetch(docUrl: Sesame#URI): Try[HttpResponse[scala.util.Try[Sesame#Graph]]] = {
  assert (docUrl == docUrl.fragmentLess)
  Try( //we want to catch connection exceptions
   Http(docUrl.toString)
    .header("Accept", "application/rdf+xml,text/turtle,application/ld+json,text/n3;q=0.2")
    .exec { case (code, headers, is) =>
      headers.get("Content-Type")
             .flatMap(_.headOption)
             .fold[Try[(io.RDFReader[Sesame, Try, _],String)]](
                   Failure(new java.lang.Exception("Missing Content Type"))
              ) { ct =>
        val ctype = ct.split(';')
        val parser = ctype(0).trim match {
          case "text/turtle" => Success(turtleReader)
          case "application/rdf+xml" => Success(rdfXMLReader)
          case "application/n-triples" => Success(ntriplesReader)
          case "application/ld+json" => Success(jsonldReader)
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
        parser.read(new java.io.InputStreamReader(is, encoding), docUrl.toString)
      }
    }).recoverWith{case t => Failure(IOException(docUrl,t))}
}

import scala.concurrent.{Future,ExecutionContext}
import java.util.concurrent.ThreadPoolExecutor
val threadPooleExec = new ThreadPoolExecutor(2,50,20,java.util.concurrent.TimeUnit.SECONDS,new java.util.concurrent.LinkedBlockingQueue[Runnable])
implicit lazy val webcontext = ExecutionContext.fromExecutor(threadPooleExec,err=>System.out.println("web error: "+err))

case class HttpException(docURL: Sesame#URI, code: Int, headers: Map[String, IndexedSeq[String]]) extends java.lang.Exception
case class ParseException(docURL: Sesame#URI, parseException: java.lang.Throwable) extends java.lang.Exception

class Cache(implicit val ex: ExecutionContext) {
   import scala.collection.mutable.Map
   val db = Map[Sesame#URI, Future[HttpResponse[Try[Sesame#Graph]]]]()
   def get(uri: Sesame#URI): Future[HttpResponse[Try[Sesame#Graph]]] = {
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

  def getPointed(uri: Sesame#URI): Future[PointedGraphWeb] =
     get(uri).flatMap{ response =>
         if (! response.is2xx ) Future.failed(HttpException(uri, response.code, response.headers))
         else {
           val moreTryInfo = response.body.recoverWith{ case t => Failure(ParseException(uri,t)) }
           Future.fromTry(moreTryInfo).map{g=>
               PointedGraphWeb(uri.fragmentLess,PointedGraph[Sesame](uri,g),response.headers.toMap)
           }
        }
     }

  case class PointedGraphWeb(webDocURL: Sesame#URI,
                           pointedGraph: PointedGraph[Sesame],
                           headers: scala.collection.immutable.Map[String,IndexedSeq[String]]) {
        def jump(rel: Sesame#URI): Seq[Future[PointedGraphWeb]] =
          (pointedGraph/rel).toSeq.map{ pg =>
              if (pg.pointer.isURI) Cache.this.getPointed(pg.pointer.asInstanceOf[Sesame#URI])
              else Future.successful(this.copy(pointedGraph=PointedGraph[Sesame](pg.pointer, pointedGraph.graph)))
       }
    }
 }

def sequenceNoFail[A](seq :Seq[Future[A]]): Future[Seq[Try[A]]] = 
     Future.sequence{ seq.map(_.transform(Success(_)))}

def conscientiousFriend(webID: Sesame#URI)(implicit cache: Cache): Future[Seq[(Sesame#URI, Option[PointedGraph[Sesame]])]] = {
    for{
      friendsFuture: Seq[Future[cache.PointedGraphWeb]] <- cache.getPointed(webID).map(_.jump(foaf.knows))
      triedFriends: Seq[Try[cache.PointedGraphWeb]] <- Future.sequence(
        friendsFuture.map (_.transform(tryPgw => Success(tryPgw)))
      )
    } yield {
      triedFriends.collect {
        case Success(cache.PointedGraphWeb(doc,pg,_)) =>
        val evidence = (pg/foaf.knows).filter((friend: PointedGraph[Sesame]) => webID == friend.pointer)
        (doc, evidence.headOption)
      }
    }
}
