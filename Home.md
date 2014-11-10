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

[Getting started](https://github.com/w3c/banana-rdf/wiki/Getting-started)

[Main features](https://github.com/w3c/banana-rdf/wiki/Features)

[Core concepts](https://github.com/w3c/banana-rdf/wiki/Core-concepts)

[Using banana-rdf with Sesame](https://github.com/w3c/banana-rdf/wiki/Sesame)

[Using banana-rdf with Jena](https://github.com/w3c/banana-rdf/wiki/Jena)

[Using Banana-rdf in the browser](https://github.com/w3c/banana-rdf/wiki/Scalajs)

[Writing banana support for your RDF library](https://github.com/w3c/banana-rdf/wiki/Core-concepts)