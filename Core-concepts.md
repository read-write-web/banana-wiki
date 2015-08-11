...or one RDF library to rule 'em all!
--------------------------------------

The main idea behind banana-rdf is to make your code as generic is possible, so sometimes only several lines of code are specific to RDF database of your chose and you can switch easily. 


When comparing tests of different underlying RDF implementations, like
 [Sesame ](https://github.com/w3c/banana-rdf/blob/master/sesame/src/test/scala/org/w3/banana/sesame/SesameSparqlEngineTest.scala) and
 [Jena](https://github.com/w3c/banana-rdf/blob/master/jena/src/test/scala/org/w3/banana/jena/JenaSparqlEngineTest.scala) it is easy to
spot that there are only several lines of code there, so all tests (with a lot of complex stuff that is checked there) are defined in   generic abstract classes that have nothing to do with implementations!
  It reveals the core principle of BananaRDF: `once written, your code will work for all supported RDF libraries with only minor changes and you can switch easily whenever you want to or add your own implementation`.

__NOTE: until we write thorough documentation, tests are the best place to understand__
__what you can do and how [test__
__suite](https://github.com/w3c/banana-rdf/tree/master/rdf-test-suite).__ 

How was it possible to achieve this?  To add support of your own RDF library you just have to extend:
   [RDF trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/RDF.scala) ,
    [RDF operations trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/RDFOps.scala),
    and [Sparql operations trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/SparqlOps.scala)

 In general there are two common ways of implementing a generic library that supports many implementations:
  1. Convince them all to adopt common interfaces. In reality, it is hard to achieve as negotiations with libraries maintainers are required and often such implementations are not full.
  2. Wrap classes of other libraries into your own classes. In such case no negotiations are needed, but
  wrapping takes efforts and is not good for performance and code clarity. 

'banana-rdf' uses neither of them! By leveraging such cool features of scala like type classes, implicits and type aliases it is possible to add support of many implementations without wrapping and negotiations.

Let's understand how it works! 
Banana-rdf heavily relies on type aliases and type classes. If you have not heard about the concept of type classes before it is recommended to read some articles (like [this one](http://danielwestheide.com/blog/2013/02/06/the-neophytes-guide-to-scala-part-12-type-classes.html)) or watch some videos (like [this one](https://www.youtube.com/watch?v=CCsGHPxA9E0)) about them before reading further. 
As an example let's look how Sesame support is done. The most important trait of banana-rdf is an
[RDF trait](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf/common/src/main/scala/org/w3/banana/RDF.scala) that contains nothing but a type aliases to be overriden. Each implementation (Jena, Sesame, Plantain, N3js, etc) overrides this trait with its own native types. In case of Sesame it looks like this:

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
The implicit resolution in Scala works in a way that it tries to resolve missing implicit from companion object of generic parameters that are passed to generic methods. That means that you can define a lot of implicits in some companion object of some class and pass this class as type parameter to generic methods only to get implicits from it resolved. This trick is heavily used to implement a type-class pattern in Scala. In case of banana-rdf, Sesame (and also Jena,Plantain and other traits that extends RDF class) has a Sesame companion object that has a lot of implicits with different operations inside.
A common way of working is based on usage of these generic "Operation traits". Such traits have a generic RDF parameter that is used to resolve their implementations from a companion object of RDF type class. 
Operations objects are used for two purposes: they make different operations with RDF data and they have some useful inside that add "syntax sugar" to the code.

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
Here it is easy to see that RDF class with type is passed. RDFOps has no knowledge of implementation types, it just gets type aliases and can only differentiate between triplets, graphs, solutions and manipulates them when it can.
 So, Sesame support includes passing a class that contains type aliases for sesame RDF types and some extra methods for functions that cannot be defined without knowing Sesame peculiarities. As type aliases are used, no wrapping is involved, everything that is returned is just native Sesame types!

 ```scala
 class SesameOps extends RDFOps[Sesame] with DefaultURIOps[Sesame] {

   val valueFactory: ValueFactory = ValueFactoryImpl.getInstance()

   //a lot of other code with implementations of abstract methods
}
```
With this approach, only a small amount of code is required for each RDF implementation and most of the code is generic.
That means that banana team can (and does) have a lot of time for features, so many banana-rdf classes are about adding additional operations/syntax, DSL and other pluggable modules that are generic and thus work for all implementations.