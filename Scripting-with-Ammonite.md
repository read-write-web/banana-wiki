[Ammonite](http://ammonite.io/) is new Scala based shell, a typesafe replacement of bash, that makes scripting fun again. This wiki page collects some of the things one needs to get going with Ammonite and banana-rdf.  It hopefully will lead to improvements to banana-rdf to make working in ammonite easier. 

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
point onto a node to explore it. (The turtle parser is inherited by the ops defined in the sesame case
[in the SesameModule](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/sesame/src/main/scala/org/w3/banana/sesame/SesameModule.scala)

```scala
> val hg = turtleReader.read(new java.io.StringReader(henryDoc.body), henryDocUrl)
 hg: scala.util.Try[Sesame#Graph] = Success(
  [(http://axel.deri.ie/~axepol/foaf.rdf#me, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person) [null], (http://axel.deri.ie/~axepol/foaf.rdf#me, http://xmlns.com/foaf/0.1/name, "Axel Polleres"^^<http://www.w3.org/2001/XMLSchema#string>) [null],
...
> val pg = PointedGraph[Sesame](URI(henryDocUrl+"#me"),hg.get)
pg: PointedGraph[Sesame] = org.w3.banana.PointedGraph$$anon$1@6a39a42c
> val k = pg/foaf.knows
k: PointedGraphs[Sesame] = PointedGraphs(
  org.w3.banana.PointedGraph$$anon$1@53dc5333,
  org.w3.banana.PointedGraph$$anon$1@787fdb85,
...
> (k/foaf.name).map(_.pointer)
res45: Iterable[Sesame#Node] = List(
  "Axel Polleres"^^<http://www.w3.org/2001/XMLSchema#string>,
  "Christoph  Görn"^^<http://www.w3.org/2001/XMLSchema#string>,
...
```


