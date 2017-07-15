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
  def map[B](f: T=>B): Link[B] = this match {
     case ImageLink(tag, uri) => ImageLink(tag, f(uri))
     case HyperLink(tag, uri) => HyperLink(tag, f(uri))
  }
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


