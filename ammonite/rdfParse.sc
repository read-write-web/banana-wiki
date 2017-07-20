import $exec.akkaHttp


def proxyRdfa(uri: AkkaUri): Future[PGWeb] = {
    type PGWeb = IRepresentation[PointedGraph[Rdf]]
    web.pointedGET(uri)
}
