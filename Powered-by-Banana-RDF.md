## Libraries

Libraries that complement banana-rdf directly.

* [scala-js-binding](https://github.com/antonkulaga/scala-js-binding): ScalaJS html binding library, uses banana-rdf to bind html properties to RDFGraph
* [type provider examples](https://github.com/travisbrown/type-provider-examples): turns ontologies into 

## Applications

applications that use banana-rdf 

### Read Write Web
* [rww-play](https://github.com/read-write-web/rww-play/) a server implementing the [Linked Data Platform](http://www.w3.org/TR/ldp/) and using [WebID Authentication](http://webid.info/spec/)
* [rww-scala-js](https://github.com/read-write-web/rww-scala-js) a scala-js browser application that consumes Linked Data and produces a User Interface

### Web applications & UI

* [forms play](https://github.com/jmvanel/semantic_forms/tree/master/scala/forms_play), a quite generic Play! application to navigate the LOD and edit backed by a Jena TDB SPARQL base
* [forms](https://github.com/jmvanel/semantic_forms/tree/master/scala/forms), a form generator with HTML rendering and RDF configuration, used by the preceding project
* [corporate risk](https://github.com/jmvanel/corporate_risk), input forms and results management for corporate risk and immaterial capital evaluation; leveraging on semantic_forms

## Data servers

* [cityData](https://github.com/pixelhumain/cityData) a very simple REST read-only wrapper for a SPARQL embedded database: from e.g. `HTTP GET /cities/fra/01600` , output JSON-LD for triples
`dbpedia:Reyrieux ?P ?O .` . It is a simple example of a Play! application that is written database independent, with 2 concrete applications: 1) with Apache Jena TDB 2) with BlazeGraph (formerly BigData(R() )

## Companies & Projects

* [Shelley-Godwin Archive](http://mith.umd.edu/research/project/shelley-godwin-archive/) used banana-rdf according to [Travis Brown](/travisbrown)
* [CoBusiness](http://www.cobusiness.fr/) a site about barter for professionals, according to [Jean Marc Vanel](/jmvanel)