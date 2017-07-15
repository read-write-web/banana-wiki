import ammonite.ops._
import scala.util.matching.Regex
import $ivy.`com.typesafe.akka::akka-http:10.0.9`
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import java.net.URI
import scala.util.Try

sealed trait Link[T]{
  val tag: String
  val uri: T
}

case class ImageLink[T](tag: String, uri: T) extends Link[T]
case class HyperLink[T](tag: String, uri: T) extends Link[T]

def gatherLinks(wikiLine: String): Seq[Link[String]] = {
  val patternForLinks = """`[^`]*`|(!)?\[([^\[\]]+)\]\(([^\(\)]+)\)""".r
  val iterator = patternForLinks findAllMatchIn wikiLine

  iterator.toSeq.map(_.subgroups).collect {
    case "!" :: tag :: url :: Nil => ImageLink[String](tag , url)
    case null :: tag :: url :: Nil if(tag != null && url != null) =>
                                     HyperLink[String](tag , url)
  }
}

def makeAkkaUri(link: Link[String]): Try[Link[Uri]] = link match {
  case ImageLink(tag, uri) => Try(ImageLink(link.tag, Uri(link.uri)))
  case HyperLink(tag, uri) => Try(HyperLink(link.tag, Uri(link.uri)))
}

def makeJavaUri(link: Link[String]): Try[Link[URI]] = link match {
  case ImageLink(tag, uri) => Try(ImageLink(link.tag, URI.create(link.uri)))
  case HyperLink(tag, uri) => Try(HyperLink(link.tag, URI.create(link.uri)))
}
