...or one RDF library to rule 'em all!
--------------------------------------

Until we write thorough documentation, the best place to understand
what you can do is to go through the [test
suite](https://github.com/w3c/banana-rdf/tree/master/rdf-test-suite).

When comparing tests of different underlying RDF implementations, like
 [Sesame ](https://github.com/w3c/banana-rdf/blob/master/sesame/src/test/scala/org/w3/banana/sesame/SesameSparqlEngineTest.scala) and
 [Jena](https://github.com/w3c/banana-rdf/blob/master/jena/src/test/scala/org/w3/banana/jena/JenaSparqlEngineTest.scala) it is easy to
  spot that there are only several lines of code there, so all tests (with a lot of complex stuff that is checked there) are defined in
  generic abstract classes that have nothing to do with implementations!
  It reveals the core principle of BananaRDF: `once written, your code will work for all supported RDF libraries with only minor changes
 and you can switch easily whenever you want to or add your own implementation`.

 How was it possible to achieve this?  To add support of your own RDF library you just have to extend:
   [RDF trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/RDF.scala) ,
    [RDF operations trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/RDFOps.scala),
    and [Sparql operations trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/SparqlOps.scala)

 In general there are two common ways of implementing a generic library that supports many implementations
  1) Convince them all to adopt common interfaces. In reality it is hard to achieve as negotiations with libraries maintainers are required
  and often such implementations are not full.
  2) Wrap classes of other libraries into your own classes. In such case no negotiations are needed, but
  wrapping takes efforts and is not good for performance and code clarity.

  'banana-rdf' uses neither of them! By leveraging such cool features of scala like type classes, implicits and type aliases it is possible
  to add support of many implementations without wrapping and negotiations.

As an example let's look how Sesame support is done. RDF trait is a class containing type aliases. Sesame trait just assigns
them to corresponding type aliases, here is part of its code:

  ```scala
    trait Sesame extends RDF {
      // types related to the RDF datamodel
      type Graph = Model
      type Triple = Statement
      type Node = Value
      type URI = SesameURI
      type BNode = SesameBNode
      type Literal = SesameLiteral
      type Lang = String

    // + one some extra lines of code
   }
  ```
Then this class with type aliases is used in so-called "Operation traits". What is interesting about this is that many of them
are written without even knowing what sesame classes are about!
Most of common banana-rdf classes are about supporting different operations that can be done with RDF as well as some syntax support.
Most of them are defined in a generic way:

```scala
trait RDFOps[Rdf <: RDF]
    extends URIOps[Rdf]
    with RDFDSL[Rdf]
    with CommonPrefixes[Rdf]
    with syntax.RDFSyntax[Rdf] {

  // graph

  def emptyGraph: Rdf#Graph

  def makeGraph(it: Iterable[Rdf#Triple]): Rdf#Graph

  def getTriples(graph: Rdf#Graph): Iterable[Rdf#Triple]

  // + some other code
  }
```
Here it is easy to see that RDF class with type is passed. RDFOps has no knowledge of implementation types, it just gets type aliases
 and can only differentiate between triplets, graphs, solutions and manipulates them when it can.
 So, Sesame support includes passing a class that contains type aliases for sesame RDF types and some extra methods for functions
 that cannot be defined without knowing Sesame peculiarities. As type aliases are used, no wrapping is involved,
 everything that is returned is just native Sesame types!

 ```scala
 class SesameOps extends RDFOps[Sesame] with DefaultURIOps[Sesame] {

   val valueFactory: ValueFactory = ValueFactoryImpl.getInstance()

   //a lot of other code with implementations of abstract methods
}
```
With this approach only a small amount of code is required for each RDF implementation and most of the code is generic.
That means that banana team can (and does) have a lot of time for features, so many banana-rdf classes are about adding
additional operations/syntax, DSL and other plugable modules that are generic and thus work for all implementations.