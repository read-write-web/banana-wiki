import coursier.core.Authentication, coursier.MavenRepository
import scala.concurrent.ExecutionContext
import com.typesafe.config._

import $ivy.`org.w3::banana-sesame:0.8.4`
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
import akka.event.Logging

import scala.concurrent.Future
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal
import scala.util.{Try,Success,Failure}

import ammonite.ops._
import scala.util.matching.Regex
import $ivy.`com.typesafe.akka::akka-http:10.0.9`
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import java.net.URI
import scala.util.Try
import scala.collection.immutable

implicit val system = ActorSystem("akka_ammonite_script")
val log = Logging(system.eventStream, "Link-checker")
implicit val materializer = ActorMaterializer(
                ActorMaterializerSettings(system).withSupervisionStrategy(Supervision.resumingDecider))
implicit val ec: ExecutionContext = system.dispatcher

sealed trait Link[T]{
  val tag: String
  val uri: T
  def map[B](f: T=>B): Link[B] = this match {
     case ImageLink(tag, uri) => ImageLink(tag, f(uri))
     case HyperLink(tag, uri) => HyperLink(tag, f(uri))
     case OtherLink(tag, url) => OtherLink(tag, f(url))
  }
}

case class ImageLink[T](tag: String, uri: T) extends Link[T]
case class HyperLink[T](tag: String, uri: T) extends Link[T]
case class OtherLink[T](tag: String = "", uri: T) extends Link[T]

def gatherLinks(wikiLine: String): Seq[Link[String]] = {
  val patternForLinks = """`[^`]*`|(!)?\[([^\[\]]+)\]\(([^\(\)]+)\)|((https|http|ftp)://.*[^\\s+]\/)""".r
  val iterator = patternForLinks findAllMatchIn wikiLine
  //Patten fix

  iterator.toSeq.map(_.subgroups).collect {
    case "!" :: tag :: url :: null :: null :: Nil => ImageLink[String](tag , url)
    case null :: tag :: url :: null :: null :: Nil if(tag != null && url != null) =>
                                     HyperLink[String](tag , url)
    case null :: null :: null :: url :: iCode :: Nil if(url != null) =>
                                     OtherLink[String]("", url)
  }
}

def uriSource(link: Link[AkkaUri]*): Source[Link[AkkaUri],NotUsed] =
     Source(immutable.Seq(link:_*).to[collection.immutable.Iterable])

import akka.http.scaladsl.model.headers.Accept
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.MediaRanges
val star_star = akka.http.javadsl.model.MediaRanges.ALL.asInstanceOf[MediaRanges.PredefinedMediaRange]
val image_star = akka.http.javadsl.model.MediaRanges.ALL_IMAGE.asInstanceOf[MediaRanges.PredefinedMediaRange]


def mapToHttp(link: Link[AkkaUri]): (HttpRequest, Link[AkkaUri])  = link match {
  case ImageLink(tag, uri) => (HttpRequest(uri = link.uri).
        addHeader(Accept(image_star)), link)
  case HyperLink(tag, uri) => (HttpRequest(uri = link.uri).
        addHeader(Accept(star_star)), link)
  case OtherLink(tag, url) => (HttpRequest(uri = link.uri).
        addHeader(Accept(star_star)), link)
}


def convertLinksToSource(links: Seq[Link[String]]): Source[(HttpRequest, Link[AkkaUri]),NotUsed] = {
  val akkaUris: Seq[Link[AkkaUri]] = links.map(link => link.map(Uri(_)))
  val httpSeq: Seq[(HttpRequest, Link[AkkaUri])] = akkaUris.map(mapToHttp(_))
  val httpIterable = httpSeq.to[collection.immutable.Iterable]
  Source(httpIterable)
}

def createFlow(httpSource: Source[(HttpRequest, Link[AkkaUri]),NotUsed]):
                          Source[(Try[HttpResponse], Link[Uri]), NotUsed] = {

  val pool = Http().superPool[Link[Uri]]()
  httpSource.via(pool)
}

def browseThrough(flow: Source[(Try[HttpResponse], Link[Uri]),NotUsed]) {
  flow.runForeach {
    case (Success(HttpResponse(status, headers, entity, protocol)), link) =>
          println(s"Url: $link.uri. Status code - $status")
          if(status.isRedirection){
            for(header <- headers){
              if(header.is("location")){
                val relocated: String = header.value
                println("Relocated to: " + relocated)
                val gatheredLinks: Seq[Link[String]] = gatherLinks(relocated)
                val newlinksToSource = convertLinksToSource(gatheredLinks)
                val flowCreated: Source[(Try[HttpResponse], Link[Uri]), NotUsed] = createFlow(newlinksToSource)
                browseThrough(flowCreated)
              }
            }
          }
    case (Failure(e) , link) =>
          println(s"Url: $link.uri Error type $e")
    }
}

import java.net._
import java.io._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source.{fromInputStream}
def checkLinks(path: Path){
    /*val siteUrl: URL = new URL(url)
    val siteContent = fromInputStream( siteUrl.openStream ).getLines.mkString("\n")
    //loop this in some way
    val collected = gatherLinks(read(siteContent))
    val convertedToSource = convertLinksToSource(collected)
    val flow = createFlow(convertedToSource)
    browseThrough(flow)
  }*/
  //path: Path
  (ls! path).foreach{x =>
      if(x.ext == "md") {
        println(x)
        val collected = gatherLinks(read(x))
        val convertedToSource = convertLinksToSource(collected)
        val flow = createFlow(convertedToSource)
        browseThrough(flow)
    }
  }
}
