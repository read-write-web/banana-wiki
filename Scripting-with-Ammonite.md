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

> res15.toSet.map{ pg: PointedGraph[Sesame] => pg.pointer }
res17: Set[Sesame#Node] = Set("Alexandre"@fr)
```

