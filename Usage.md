Let's start from the simple example, making a query to remote endpoint. At first you have to include banana-rdf dependencies to your project. The code will be pretty much the same for all implementation, for the sake of simplicity let's try jena's one:
```
libraryDependencies += "org.w3" %% "jena" % "0.7"
```
__Note: package names will change in a next version of banana-rdf.

banana-rdf comes with so-called modules, generic traits that have some functionality inside.
First let's define a number of modules for the task. 

```scala
trait SPARQLExampleDependencies
  extends RDFModule
  with RDFOpsModule
  with SparqlOpsModule
  with SparqlHttpModule
```
A module is nothing more then a class that contains some useful types and implicits. For instance SparqlOpsModule contains just one implicit value that allows to do sparql operations on rdf graph.
```scala
  implicit val sparqlOps: SparqlOps[Rdf]
```

Then, let's write some actual code. Let's inherit from our SPARQLExampleDependencies trait and import some implicits to the code scope.

```scala
trait SPARQLExample extends SPARQLExampleDependencies { self =>

  import ops._
  import sparqlOps._
  import sparqlHttp.sparqlEngineSyntax._
```
The implictis are taken from modules that we just inherited. Each of them allow some operations that we can do with RDF graph, RDF store or sparql endpoint. So, you can get the features that you need by mixin modules with appropriate implicits. 
Note, that our code is generic right now as we did not inherit from any rdfstore implementations, all features that we get we get from implicits. The reason why we prefer implicits to inheritance is because implicits will be resolved from rdfstore implementation (i.e. Jena,Sesame,Plantain, etc) that we will mix later.
Let's define the main method of our example. There we will make a test select to dbpedia endpoint to fetch some linked data from Wikipedia and print the result.
```

  def main(args: Array[String]): Unit = {

    /* gets a SparqlEngine out of a Sparql endpoint */

    val endpoint = new URL("http://dbpedia.org/sparql/")

    /* creates a Sparql Select query */

    val query = parseSelect("""
PREFIX ont: <http://dbpedia.org/ontology/>
SELECT DISTINCT ?language WHERE {
 ?language a ont:ProgrammingLanguage .
 ?language ont:influencedBy ?other .
 ?other ont:influencedBy ?language .
} LIMIT 100
""").get

    /* executes the query */

    val answers: Rdf#Solutions = endpoint.executeSelect(query).getOrFail()

    /* iterate through the solutions */

    val languages: Iterator[Rdf#URI] = answers.iterator map { row =>
      /* row is an Rdf#Solution, we can get an Rdf#Node from the variable name */
      /* both the #Rdf#Node projection and the transformation to Rdf#URI can fail in the Try type, hense the flatMap */
      row("language").get.as[Rdf#URI].get
    }

    println(languages.to[List])
  }

}
```
Common way of dealing with SPARQL in bananardf is that you first parse the query with parseSelect function that you get from your sparql module and then execute your query on an endpoint and get a Future with results

And now here is the time for adding concrete rdfstore implementation. The implicits that you used earlier (like: ops, sparqlOps,  sparqlHttp.sparqlEngineSyntax) are resolved mostly from the implementation module (JenaModule in our case).


```scala
import org.w3.banana.jena.JenaModule

object SPARQLExampleWithJena extends SPARQLExample with JenaModule
```
As you can see only two lines in all code is specific to rdf store implementation and you can switch to any other implementation (Sesame, Platain, etc.) by changing several lines. 

P.S. Above-mentioned code is in [banana-rdf examples](https://github.com/w3c/banana-rdf/tree/master/examples/src/main/scala/org/w3/banana/examples), so you can easily try it yourself.