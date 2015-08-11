`banana-rdf` is a library for RDF, SPARQL and Linked Data technologies
in Scala.

It can be used with existing libraries without any added cost. There
is no wrapping involved: you manipulate directly the real objects. We
currently support Jena, Sesame and Plantain, a pure Scala
implementation.

With `banana-rdf`, you get `Diesel`, a nice DSL to build and navigate
within **pointed graphs** (graphs with a pointer to an inner
node). You also get an abstraction for **graph stores**
(`GraphStore`), which do not have to be **SPARQL engines**
(`SparqlEngine`). Of course, you can **serialize** and **deserialize**
most of the RDF syntaxes as well as JSON-LD (RDFa will come soon) [read more about features here](https://github.com/w3c/banana-rdf/wiki/Features)

Documentation
=============

[Main features](https://github.com/w3c/banana-rdf/wiki/Features)

### Using banana-rdf

[Core concepts](https://github.com/w3c/banana-rdf/wiki/Core-concepts)

[Making a SPARQL request](https://github.com/w3c/banana-rdf/wiki/Usage-SPARQL)

[Reading and writing classes to RDF graph](https://github.com/w3c/banana-rdf/wiki/Usage-Binders)

[Writing and reading RDF files](https://github.com/w3c/banana-rdf/wiki/Usage-IO)

### Hacking banana-rdf
[Getting started](https://github.com/w3c/banana-rdf/wiki/Getting-started)

### Videos 

Two presentations at Scala Conferences should help get you going.

An overview of RDF and Linked Data its uses and some core concepts of banana-rdf were given at Scala-eXchange 2014.

[![skillsmatter video: building a secure social web using scala and scala-js](https://cloud.githubusercontent.com/assets/124506/5917678/facf06b0-a61f-11e4-97fd-2457f26a46b2.png)](https://skillsmatter.com/skillscasts/5960-building-a-secure-distributed-social-web-using-scala-scala-js)

Alexandre Bertails dug much deeper into the structure of banana-rdf at his talk at ScalaDays 2015 in San Francisco. 

[![Interacting with the Web of data in Scala ](http://bblfish.net/tmp/2015/06/23/scaladaysSF2015.png)](https://www.parleys.com/tutorial/banana-rdf-interacting-web-data-scala)


### Powered by Banana-RDF

See [[Powered by Banana-RDF]]