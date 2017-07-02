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

  interp.repositories() ++= Seq(MavenRepository(
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

Next we are going to try building a graph, taking code from [the diesel example](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf-test-suite/shared/src/main/scala/org/w3/banana/diesel/DieselGraphConstructTest.scala) 

```scala
> import org.w3.banana._

> import org.w3.banana.syntax._

> import org.w3.banana.sesame.Sesame.ops

val g: PointedGraph[Sesame] = (
               bnode("betehess")
               -- foaf.name ->- "Alexandre".lang("fr")
               -- foaf.title ->- "Mr"
             )
g: PointedGraph[Sesame] = org.w3.banana.PointedGraph$$anon$1@21e9fd9e

> g/foaf.name
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
 * if a link goes bad, he can remember who was at the end of the link
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



# Todo

