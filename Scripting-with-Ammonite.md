[Ammonite](http://ammonite.io/) is new Scala based shell, a typesafe replacement of bash, that makes scripting fun again. In particular it should prove to be a great tool to start exploring the semantic web, as if it were part of your file system, write some small initial scripts to try out ideas, and actually make the samantic web part of your scripting environment.

 This wiki page collects some of the things one needs to get going with Ammonite and banana-rdf.  It hopefully will lead to improvements to banana-rdf to make working in Ammonite easier. 

# Ammonite and Banana-RDF

1) Download [Ammonite Shell](http://ammonite.io/#Ammonite-Shell) as described on their documentation page
  
   For Windows Users check [this blog post at blog.ssanj.net](http://blog.ssanj.net/posts/2016-02-24-running-ammonite-on-windows-with-conemu.html) and [Ammonite issue 119](https://github.com/lihaoyi/Ammonite/issues/119) and please report any success to us on the gitter channel and if possible a pointer to a howto for it.

2) start Ammonite 

3) run the following at the `>` command prompt, which will be
specific to your environment:

```scala
> import coursier.core.Authentication, coursier.MavenRepository

> interp.repositories() ++= Seq(MavenRepository(
  "http://bblfish.net/work/repo/snapshots/"
  ))

> import $ivy.`org.w3::banana:0.8.4-SNAPSHOT`

> import $ivy.`org.w3::banana-rdf:0.8.4-SNAPSHOT`

> import $ivy.`org.w3::banana-sesame:0.8.4-SNAPSHOT`

```

those last imports will download a lot of libraries the first time round. Here we are
choosing to use the Sesame implementation of banana. You could use another one, such as
Jena.

# Constructing and querying RDF Graphs

Next we are going to try building a graph, taking code from [the diesel example](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf-test-suite/shared/src/main/scala/org/w3/banana/diesel/DieselGraphConstructTest.scala). First we import
the classes and functions we need.

```scala
> import org.w3.banana._

> import org.w3.banana.syntax._

> import org.w3.banana.sesame.Sesame.ops

> import ops._
```

Then we import the [foaf ontology](http://xmlns.com/foaf/0.1/) identifiers that
are predefined for us in the [banana prefix file](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf/shared/src/main/scala/org/w3/banana/Prefix.scala) as they
are so useful in examples. This makes a lot easier to read than having to write URIs out
completely.

```scala
> val foaf = FOAFPrefix[Sesame]
> val alex: PointedGraph[Sesame] = (
               bnode("betehess")
               -- foaf.name ->- "Alexandre".lang("fr")
               -- foaf.title ->- "Mr"
             )
alex: PointedGraph[Sesame] = org.w3.banana.PointedGraph$$anon$1@21e9fd9e
```

This Domain Specific Language (DSL) produces a [PointedGraph](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf/shared/src/main/scala/org/w3/banana/PointedGraph.scala) which is just
a graph and a pointer into the graph.

This pointed graph has as subject a blank node internally named "betehess" which has 
two relations `foaf.knows` and `foaf.title` to literals. The syntax is meant
to be somewhat reminiscent of the [Turtle](https://www.w3.org/TR/turtle/) format.

We can output that graph consisting of two triples in what is conceptually
the simplest of all RDF formats: [NTriples](https://www.w3.org/TR/n-triples/).

```Scala
> ntriplesWriter.asString(alex.graph,"")
res49: scala.util.Try[String] = Success(
  """_:betehess <http://xmlns.com/foaf/0.1/title> "Mr"^^<http://www.w3.org/2001/XMLSchema#string> .
_:betehess <http://xmlns.com/foaf/0.1/name> "Alexandre"@fr ."""
)
```

The easiest format to write is the above mentioned [Turtle](https://www.w3.org/TR/turtle/) format,
and you can see how the output here is somewhat similar to the Diesel banana-rdf DSL.

```Scala
> turtleWriter.asString(alex.graph,"")
res50: scala.util.Try[String] = Success(
  """
_:betehess <http://xmlns.com/foaf/0.1/title> "Mr" ;
	<http://xmlns.com/foaf/0.1/name> "Alexandre"@fr .
"""
)
```

Next we can explore the graph in a way that is somewhat reminiscent of OO programming,
but with the dot `.` notation replaced with a `/` notation. As RDF relations when starting
from a node are one to many, we receive not just one PointedGraph back but a sequence of them
, which is what the (PointedGraphs)(https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf/shared/src/main/scala/org/w3/banana/PointedGraphs.scala) - notice the
final 's' - is for.

```Scala
> alex/foaf.name
res15: PointedGraphs[Sesame] = PointedGraphs(org.w3.banana.PointedGraph$$anon$1@432f1d0a)

> res15.map( _.pointer )
res17: Iterable[Sesame#Node] = List("Alexandre"@fr)
```

# Working with Graphs on the Web

Building our own graph and querying it is not very informative. 
So let's try getting some information from the world wide web.

First let us load a simple Scala wrapper around the Java HTTP library,
[scalaj-http](https://github.com/scalaj/scalaj-http).

```scala
> interp.load.ivy("org.scalaj" %% "scalaj-http" % "2.3.0")
```

We can now start using banana-rdf on real data.

```scala
> import scalaj.http._
import scalaj.http._
> val henryDocUrl = "http://bblfish.net/people/henry/card"
henryDocUrl: String = "http://bblfish.net/people/henry/card
>  val henryDocReq = Http(henryDocUrl)
henryDoc: HttpRequest = HttpRequest(
  "http://bblfish.net/people/henry/card",
  "GET",
  DefaultConnectFunc,
  List(),
  List(("User-Agent", "scalaj-http/1.0")),
  List(
    scalaj.http.HttpOptions$$$Lambda$112/836829272@2cfc7cdf,
    scalaj.http.HttpOptions$$$Lambda$113/414197855@29a76f28,
    scalaj.http.HttpOptions$$$Lambda$114/1232880785@40ad74ca
  ),
  None,
  "UTF-8",
  4096,
  QueryStringUrlFunc,
  true
)
> val henryDoc = henryDocReq.asString
henryDoc: HttpResponse[String] = HttpResponse(
  """@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix : <http://xmlns.com/foaf/0.1/> .
@prefix cert: <http://www.w3.org/ns/auth/cert#> .
@prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#> .
@prefix iana: <http://www.iana.org/assignments/relation/> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix pingback: <http://purl.org/net/pingback/> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix space: <http://www.w3.org/ns/pim/space#> .

<http://axel.deri.ie/~axepol/foaf.rdf#me>
    a :Person ;
    :name "Axel Polleres" .

<http://b4mad.net/FOAF/goern.rdf#goern>
    a :Person ;
...
```

So now we have downloaded the Turtle, we just need to parse it into a graph and
point onto a node of the graph (a `PointedGraph`) to explore it. (The turtle parser 
is inherited by the `ops` we imported earlier defined in the sesame case
[in the SesameModule](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/sesame/src/main/scala/org/w3/banana/sesame/SesameModule.scala)

```scala
> val hg = turtleReader.read(new java.io.StringReader(henryDoc.body), henryDocUrl)
 hg: scala.util.Try[Sesame#Graph] = Success(
  [(http://axel.deri.ie/~axepol/foaf.rdf#me, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person) [null], (http://axel.deri.ie/~axepol/foaf.rdf#me, http://xmlns.com/foaf/0.1/name, "Axel Polleres"^^<http://www.w3.org/2001/XMLSchema#string>) [null],
...
> val pg = PointedGraph[Sesame](URI(henryDocUrl+"#me"),hg.get)
pg: PointedGraph[Sesame] = org.w3.banana.PointedGraph$$anon$1@6a39a42c
> val knows = pg/foaf.knows
knows: PointedGraphs[Sesame] = PointedGraphs(
  org.w3.banana.PointedGraph$$anon$1@53dc5333,
  org.w3.banana.PointedGraph$$anon$1@787fdb85,
...
> (knows/foaf.name).map(_.pointer)
res45: Iterable[Sesame#Node] = List(
  "Axel Polleres"^^<http://www.w3.org/2001/XMLSchema#string>,
  "Christoph  Görn"^^<http://www.w3.org/2001/XMLSchema#string>,
...
```

# Following links 

## Purpose and method

In the [Friend of a Friend](http://xmlns.com/foaf/spec/) profile we [downloaded above](http://bblfish.net/people/henry/card), Henry keeps the names of the
people he knows, so that 
 * if a link goes bad, he can remember who he intended the link to refer to (in order to fix it if possible)
 * to allow user interfaces to immediately give some information about what he was intending to link to
to, without having to downloading more information.

But most of the information is actually not in his profile - why after all should he keep his profile
up to date about where his friends live, who their friends are, what their telephone number is, 
where their blogs are located, etc.... If he did not share responsibility with others in keeping
data up to date, he would soon have to maintain all the information in the world. 

That is where linked data comes  in: it allows different people and organisations to share the burden of maintaining information. The URLs used in the names of the relations and the names
of the subjects and objects refer (directly and often indirectly via urls ending in #entities) to documents
maintained by others - in this case the people Henry knows. 

So the next step is to follow links from one resource to another, download those documents, turn them
into graphs, etc... We can do this if the pointers of the `PointedGraph` we named `knows` 
are urls that don't belong to the original  document (ie are not #urls that belong to that document, 
blank nodes or literals). Then for each such URL `url` having  downloaded the documents that those URLs point to, parsed them into a graph `g` and created a pointed graph `PointedGraph(url,g)` we can then continue
exploring the data from that location. 

In the above example we asked for the default representation of the `henryDocUrl` resource.
As it happened it returned Turtle. But as we want to follow the `knows/foaf.knows` links to other.

Let us write this then as little scripts and see how far we get.

## Fetching and parsing docs

So first of all we'd like to have one simple function that takes a URL and returns the pointed graph of that URL if successful, or some explanation of what went wrong.

A little bit of playing around and we arrive at this function, that nicely
gives us all the information we need:

```scala
> def fetch(point: Sesame#URI):  HttpResponse[scala.util.Try[PointedGraph[Sesame]]] = {
          val docUrl = point.fragmentLess.toString
          Http(docUrl).execute{ is =>
             turtleReader.read(is,point.fragmentLess.toString)
                   .map{ g => PointedGraph[Sesame](point,g) }
          }
         }
defined function fetch
```

It keeps all the headers that we received, which may be useful for the extra information they contain, it gives us the content transformed into a pointed graph or an error in case of parsing errors.

But it is not quite right. There are two problems both related to the various syntaxes that RDF can be published in:

1. As we follow links on the web we would like to tell the server we come accross what types of mime types we understand so we increase the likeleyhood that it sends one we can parse. For sesame [we currently can parse](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/sesame/src/main/scala/org/w3/banana/sesame/SesameModule.scala) the following syntaxes for RDF: [RDF/XML](https://www.w3.org/TR/rdf-syntax-grammar/) popular in the early 2000s when XML was popular, [NTriples](https://www.w3.org/TR/n-triples/) the easiest to parse, [Turtle](https://www.w3.org/TR/turtle/) the easiest to read, [json-ld](https://json-ld.org/) popular because of it's encoding in JSON.

2. When we receive the response we need to select the parser given the mime type of the document returned by the server.

```scala
def fetch(point: Sesame#URI): HttpResponse[scala.util.Try[PointedGraph[Sesame]]] = {
  val docUrl = point.fragmentLess.toString
  Http(docUrl)
    .header("Accept", "application/rdf+xml,text/turtle,text/n3;q=0.2")
    .exec { case (code, headers, is) =>
      headers.get("Content-Type").flatMap(_.headOption).fold[Try[PointedGraph[Sesame]]](Failure(new java.lang.Exception("Missing Content Type"))) { ct =>
        val ctype = ct.split(';')
        val parser = ctype(0).trim match {
          case "text/turtle" => turtleReader
          case "application/rdf+xml" => rdfXMLReader
          case "application/n-triples" => ntriplesReader
          case "application/ld+json" => jsonldReader
        }
        val attributes = ctype.toList.tail.flatMap(_.split(',').map(_.split('=').toList.map(_.trim))
        ).map(avl => (avl.head, avl.tail.headOption.getOrElse(""))).toMap
        val encoding = attributes.get("encoding").getOrElse("utf-8")
        parser.read(new java.io.InputStreamReader(is, encoding), point.fragmentLess.toString)
          .map { g => PointedGraph[Sesame](point, g) }
      }
    }
}
```

The above functions shows that dealing with the mime types is a little tricky perhaps, but
not that difficult. The code was written entirely in the Ammonite shell (and that is perhaps the
longest piece of code that makes sense to write there).

```scala
> val bblfish = fetch(URI("http://bblfish.net/people/henry/card#me"))
bblfish: HttpResponse[Try[PointedGraph[Sesame]]] = HttpResponse(
  Success(org.w3.banana.PointedGraph$$anon$1@71697aa2),
  200,
  Map(
    "Accept-Ranges" -> Vector("bytes"),
    "Access-Control-Allow-Headers" -> Vector("Origin, X-Requested-With, Content-Type, Accept"),
    "Access-Control-Allow-Origin" -> Vector("*"),
    "Connection" -> Vector("Keep-Alive"),
    "Content-Length" -> Vector("17508"),
    "Content-Location" -> Vector("card.ttl"),
    "Content-Type" -> Vector("text/turtle; charset=utf-8"),
    "Date" -> Vector("Sun, 02 Jul 2017 14:37:25 GMT"),
    "ETag" -> Vector("\"4464-52f5cde585f9e;52f5cde5e7ae3\""),
    "Keep-Alive" -> Vector("timeout=5, max=100"),
    "Last-Modified" -> Vector("Thu, 31 Mar 2016 18:59:57 GMT"),
    "Server" -> Vector("Apache/2.4.12 (Unix)"),
    "Status" -> Vector("HTTP/1.1 200 OK"),
...
```
 


# Todo

