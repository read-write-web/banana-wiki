import $exec.akkaHttp
import Web._
implicit val web = new Web()

def proxyRdfa(uri: AkkaUri): Future[PGWeb] = {
    type PGWeb = IRepresentation[PointedGraph[Rdf]]
    web.pointedGET(uri)
}
