import $file.akkaHttp, akkaHttp._

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.actor.SupervisorStrategy
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{Uri=>AkkaUri,_}

import akka.stream.{ActorMaterializer, ActorMaterializerSettings,Supervision,_}
import akka.stream.scaladsl._
import akka.{ NotUsed, Done }
import akka.event.Logging

import akkaHttp.Web._

implicit val web = new akkaHttp.Web()

import web._

def proxyRdfa(uri: AkkaUri): Future[PGWeb] = {
    type PGWeb = IRepresentation[PointedGraph[Rdf]]
    web.pointedGET(uri)
}
