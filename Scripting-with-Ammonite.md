 This wiki page explains the why and how of banana-rdf in a practical way. This will help you getting started, in an easy step by step fashion using the Ammonite command line, and start making this library real with practical and fun examples of the Semantic Web.
  
  We will explain the major concepts along the way in a step by step fashion which you can follow easily on the new Ammonite command line for yourself. The aim is to quickly get you to understand Linked Data by exploring it on the web using the banana-rdf library, so that you can start writing your own shell scripts and move on to larger programs the full value of this framework starts becoming clear.

## Why Ammonite?

[Ammonite](http://ammonite.io/) is the revolutionary Scala based shell, a typesafe replacement of bash, that makes scripting fun, safe and efficient. 

Whereas in a unix shell, processes communicate through the singly typed string interface of the unix pipe, in Ammonite they communicate using composable typed functions, that can use the vast array of libraries written for Java and Scala.   In the Unix shell each process has to parse the result of the previous as a simple string, which is both very innefficient and introduces potential dangerous errors that can only be spotted at runtime. 

The Java based environement brings full Unicode support, mulithreaded programming, and programmatic access to the web, all of which can be now used directly from the shell.

In particular it should prove to be a great tool to start exploring the semantic web, as if it were part of your file system, write some small initial scripts to try out ideas, and make it part of your scripting environment.

 
## Ammonite and Banana-RDF

1) Download [Ammonite Shell](http://ammonite.io/#Ammonite-Shell) as described on their documentation page.  For the newly released Ammonite 1.0 (4 July 2017) it is as easy as running the following two lines from the command line

```bash
$ mkdir -p ~/.ammonite && curl -L -o ~/.ammonite/predef.sc https://git.io/vHaKQ
$ sudo curl -L -o /usr/local/bin/amm https://git.io/vQEhd && sudo chmod +x /usr/local/bin/amm && amm
```
   
   but check the latest on [their excellently documented web site](http://ammonite.io/#Ammonite-Shell) - (and update this wiki if there is a change).   
  
   For Windows Users we have found that it works well with the latest versions of [Windows 10 Subsystem for Linux](https://en.wikipedia.org/wiki/Windows_Subsystem_for_Linux) that supports Ubuntu 16.04 as of the 4th July 2017. (Otherwise check out [Ammonite issue 119](https://github.com/lihaoyi/Ammonite/issues/119))

2) start Ammonite 

```bash
$ amm
```

3) Import the banana-rdf libraries. To to this run the following at the `@` command prompt, which will be specific to your environment. I have removed the `@` prompt from may of these examples to make copy and pasting easier. After each of these lines you will see a response from the command prompt.

```Scala
import coursier.core.Authentication, coursier.MavenRepository

interp.repositories() ++= Seq(MavenRepository(
  "http://bblfish.net/work/repo/snapshots/"
  ))

import $ivy.`org.w3::banana-sesame:0.8.4-SNAPSHOT`
```

those last imports will download a lot of libraries the first time round. Here we are choosing to use the Sesame implementation of banana. You could use another one, such as Jena.  

_todo: in the near future we will genericise the
code to allow you to choose which version you prefer to use in a couple of lines of code_

# Constructing and querying RDF Graphs

Next we are going to build an RDF graph. RDF, stands for Resoure Description Framework, and is used to describe things by relating them to one another. For
example we could describe the relation of Tim Berners Lee and Vint Cert as a relation `aRb` between them as shown in the picture below.

![Mini Graph of TimBl](https://raw.githubusercontent.com/wiki/banana-rdf/banana-rdf/img/VintCertTimBLHandShake.png)

But if one were just given `aRb` without the picture, how could one know what it means? Where could one look up that information? How could one create new types of relations? This is what RDF - the Resource Description Framework - solves by naming relations and things with URIs, that can, especially if they are `http` or `https` URLs be dereferences from the web - as we will see in the next section - in order to glean more information from them. 

Here for example is a more detailed graph consisting of 4 arrows describing the relation between Tim, Vint Cert, and some other entity referred to by a long URL 
[`<http://bblfish.net/people/henry/card#me>`](http://bblfish.net/people/henry/card#me). 

![Mini Graph of TimBl](https://raw.githubusercontent.com/wiki/banana-rdf/banana-rdf/img/TimBLGraph.png)

 The relations are not written out in full in the above example, as that would be awkward to read and take up a lot of space. Instead we have relations named `foaf:name` and `foaf:knows` which are composed of two parts separated by a column: a namespace part 'foaf' which stands for `http://xmlns.com/foaf/0.1/` followed by a string `name` or `knows` which can be concatenated together to form a URL, in this case [`http://xmlns.com/foaf/0.1/name`](http://xmlns.com/foaf/0.1/name) and [`http://xmlns.com/foaf/0.1/name`](http://xmlns.com/foaf/0.1/name). 

 How do we write this out in banana-rdf?

 First, we import the classes and functions we need. (I have removed the `@` command line prompt  to make it easier to copy and paste the whole lot in one go)

```Scala
import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.sesame.Sesame
import Sesame._
import ops._
```

Then we import the [foaf ontology](http://xmlns.com/foaf/0.1/) identifiers that
are predefined for us in the [banana prefix file](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf/shared/src/main/scala/org/w3/banana/Prefix.scala) as they
are so useful. This makes a lot easier to read than having to write URIs out
completely. (here to show the interactive side I have left the `@` prompt in, and the response
from running the code)

```Scala
@ val foaf = FOAFPrefix[Sesame]
foaf: FOAFPrefix[Sesame] = Prefix(foaf)
@  val timbl: PointedGraph[Sesame] = (
     URI("https://www.w3.org/People/Berners-Lee/card#i")
        -- foaf.name ->- "Tim Berners-Lee".lang("en")
        -- foaf.plan ->- "Make the Web Great Again"
        -- foaf.knows ->- (bnode("vint") -- foaf.name ->- "Vint Cerf")
        -- foaf.knows ->- URI("http://bblfish.net/people/henry/card#me")
 )
```

We can output that graph consisting of five relations (also known as "triples") in what is conceptually the simplest of all RDF formats: [NTriples](https://www.w3.org/TR/n-triples/).
(Again here the output is important, so we kept the `@` prompt. Only type the
that follows that prompt into your ammonite shell)

```Scala
@ ntriplesWriter.asString(timbl.graph,"")
res54: Try[String] = Success(
  """<https://www.w3.org/People/Berners-Lee/card#i> <http://xmlns.com/foaf/0.1/knows> <http://bblfish.net/people/henry/card#me> .
_:vint <http://xmlns.com/foaf/0.1/name> "Vint Cerf"^^<http://www.w3.org/2001/XMLSchema#string> .
<https://www.w3.org/People/Berners-Lee/card#i> <http://xmlns.com/foaf/0.1/knows> _:vint .
<https://www.w3.org/People/Berners-Lee/card#i> <http://xmlns.com/foaf/0.1/plan> "Make the Web Great Again"^^<http://www.w3.org/2001/XMLSchema#string> .
<https://www.w3.org/People/Berners-Lee/card#i> <http://xmlns.com/foaf/0.1/name> "Tim Berners-Lee"@en ."""
)
```

The easiest format for humans to read and write RDF is the [Turtle](https://www.w3.org/TR/turtle/) format, and you can see how the output here is somewhat similar to the Diesel banana-rdf DSL.

```Scala
@ turtleWriter.asString(timbl.graph,"").get
```
which will return the following Turtle:

```Turtle
<https://www.w3.org/People/Berners-Lee/card#i> <http://xmlns.com/foaf/0.1/knows>
                      <http://bblfish.net/people/henry/card#me> , _:vint ;
        <http://xmlns.com/foaf/0.1/plan> "Make the Web Great Again" ;
        <http://xmlns.com/foaf/0.1/name> "Tim Berners-Lee"@en .
_:vint <http://xmlns.com/foaf/0.1/name> "Vint Cerf" .
```

And since nothing is cool nowadays if does not produce JSON, here is the
JSON-LD rendition of it:

```Scala
jsonldCompactedWriter.asString(timbl.graph,"").get
```

which will return the following json

```javascript
{
  "@graph" : [ {
    "@id" : "_:vint",
    "http://xmlns.com/foaf/0.1/name" : "Vint Cerf"
  }, {
    "@id" : "https://www.w3.org/People/Berners-Lee/card#i",
    "http://xmlns.com/foaf/0.1/knows" : [ {
      "@id" : "http://bblfish.net/people/henry/card#me"
    }, {
      "@id" : "_:vint"
    } ],
    "http://xmlns.com/foaf/0.1/name" : {
      "@language" : "en",
      "@value" : "Tim Berners-Lee"
    },
    "http://xmlns.com/foaf/0.1/plan" : "Make the Web Great Again"
  } ]
}
```

For a full list of currently integrated serialisers look at the [SesameModule.scala](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/sesame/src/main/scala/org/w3/banana/sesame/SesameModule.scala) file.

(Note: It is very likely that there are newer json serialisers than the one that banana-rdf is using that produce much better output than this, as this has not been updated for over a year.)

The attentive reader will have noticed that the Domain Specific Language (DSL) we used above to produce those outputs returned a [PointedGraph](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf/shared/src/main/scala/org/w3/banana/PointedGraph.scala). This is an extremely simple concept best illustrated by the following diagram, namely just the pair of a graph and a pointer into the graph.

![PointedGraph TimBl](https://raw.githubusercontent.com/wiki/banana-rdf/banana-rdf/img/TimBLPointedGraph.png)

So in our `timbl` graph, `https://www.w3.org/People/Berners-Lee/card#i` is the node pointed to. 

This PointedGraph view allows us to move to an Object Oriented (OO) view of the graph. And indeed the `PointedGraph` type comes with some operations that are reminiscent of the OO dot `.` notation: the `/` for forward relation exploration and the `/-` for backward relation exploration. 


```Scala
@ timbl/foaf.knows
res58: PointedGraphs[Sesame] = PointedGraphs(
  org.w3.banana.PointedGraph$$anon$1@7d03b6aa,
  org.w3.banana.PointedGraph$$anon$1@61f7c895
)
```

Notice that if we don't give a name to a function return value Ammonite gives it a name. In this case `res58`, which we can use in the shell:

```Scala
@ res58.map(_.pointer)
res59: Iterable[Sesame#Node] = List(http://bblfish.net/people/henry/card#me, _:vint)
```

We can illustrate the above interaction with the following diagram. As you see the graph remains the same after each operation but the pointer moves.

![following the foaf:knows in timbl pointed graph](https://raw.githubusercontent.com/wiki/banana-rdf/banana-rdf/img/followingKnowsInPG.png)

Again this is very similar to OO programming when you follow an attribute to get its value.

You can explore more examples by looking at the test suite, starting from [the diesel example](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/rdf-test-suite/shared/src/main/scala/org/w3/banana/diesel/DieselGraphConstructTest.scala).

# Working with Graphs on the Web

Building our own graph and querying it is not very informative. 
So let's try getting some information from the world wide web. 

First, let us load a simple Scala wrapper around the Java HTTP library,
[scalaj-http](https://github.com/scalaj/scalaj-http).

```Scala
@ interp.load.ivy("org.scalaj" %% "scalaj-http" % "2.3.0")
```

We can now start using banana-rdf on real data. One question of interest could be what this `http://bblfish.net/people/henry/card#me` url refers to in the graph above. So let's see what the web tells us.

```Scala
@ import scalaj.http._
import scalaj.http._
@ val bblUrl = URI("http://bblfish.net/people/henry/card#me")
bblUrl: Sesame#URI = http://bblfish.net/people/henry/card#me
```

The HTTP RFC does not allow one to make requests with fragment
identifiers, since the [URI RFC](https://tools.ietf.org/html/rfc3986#section-3.5) defines fragment identifiers as

>  The fragment identifier component of a URI allows indirect
   identification of a secondary resource by reference to a primary
   resource and additional identifying information.  The identified
   secondary resource may be some portion or subset of the primary
   resource, some view on representations of the primary resource, or
   some other resource defined or described by those representations.  A
   fragment identifier component is indicated by the presence of a
   number sign ("#") character and terminated by the end of the URI.
   
In our language URIs with fragment identifiers refer to pointed graphs.
So we remove the fragment before making the HTTP request.   

```Scala
@ bblUrl.fragmentLess
res63: Sesame#URI = http://bblfish.net/people/henry/card
@ val bblReq = Http( bblUrl.fragmentLess.toString )
bblReq: HttpRequest = HttpRequest(
  "http://bblfish.net/people/henry/card",
  "GET",
  DefaultConnectFunc,
  List(),
  List(("User-Agent", "scalaj-http/1.0")),
  List(
    scalaj.http.HttpOptions$$$Lambda$111/614609966@15edb703,
    scalaj.http.HttpOptions$$$Lambda$113/286344087@5dbab6b0,
    scalaj.http.HttpOptions$$$Lambda$114/1231971264@4747e542
  ),
  None,
  "UTF-8",
  4096,
  QueryStringUrlFunc,
  true
)
```

That sets the request. The ammonite shell shows us the structure of the
request, consisting of a number of headers, including one which sets
the name of the `User-Agent` to "scalaj-http". 

Next, we make the request and retrieve the value as a string. If your internet
connection is functioning and the [bblfish.net](http://bblfish.net/) server is up, you should get something like the following result.

```
@ val bblDoc = bblReq.asString
bblDoc: HttpResponse[String] = HttpResponse(
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
..."""
```

This following illustration captures the interaction between your computer and the `bblfish.net` server when you make this request. 

![following the foaf:knows in timbl pointed graph](https://raw.githubusercontent.com/wiki/banana-rdf/banana-rdf/img/HttpGETReqResponse.png)

This illustrates the request made from JavaScript in the browser, which then displays that data using a [Scala-JS](http://scala-js.org) program available in the [rww-scala-js repository](https://github.com/read-write-web/rww-scala-js).

But before we go on writing user interfaces for people who will never know what RDF is, requiring us to work with Web Designers, artists, psychologists and others, let us stick to the command line interface. We need to learn how this works so that we can then build tools for people who don't. 

So having downloaded the Turtle, we just need to parse it into a graph and
point onto a node of the graph (a `PointedGraph`) to explore it. (The turtle parser  is inherited by the `ops` we imported earlier defined [in the SesameModule](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/sesame/src/main/scala/org/w3/banana/sesame/SesameModule.scala))

```Scala
@ val bg = turtleReader.read(new java.io.StringReader(bblDoc.body), bblUrl.fragmentLess.toString)
bg: Try[Sesame#Graph] = Success(
  [(http://axel.deri.ie/~axepol/foaf.rdf#me, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person) [null], (http://axel.deri.ie/~axepol/foaf.rdf#me, http://xmlns.com/foaf/0.1/name, "Axel Polleres"^^<http://www.w3.org/2001/XMLSchema#string>) [null], ...])
``` 

Having the graph we construct the pointed graph, allowing us then to explore it as we did in the previous section. But here we are likely to learn something new, since we did not put this data together ourselves.
  
```Scala
@ val pg = PointedGraph[Sesame](bblUrl,bg.get)
pg: PointedGraph[Sesame] = org.w3.banana.PointedGraph$$anon$1@353b86bc
@ (pg/foaf.name).map(_.pointer)
res72: Iterable[Sesame#Node] = List("Henry J. Story"^^<http://www.w3.org/2001/XMLSchema#string>)
```

So now we know the name of the person with the bblfish url. We can see who he claims to know with the following query.

```Scala
@ val knows = pg/foaf.knows
knows: PointedGraphs[Sesame] = PointedGraphs(
  org.w3.banana.PointedGraph$$anon$1@53dc5333,
  org.w3.banana.PointedGraph$$anon$1@787fdb85,
...)
```

And we can find out how many they are, what their names are,
and much more.

```Scala
@ knows.size
res71: Int = 74
@ (knows/foaf.name).map(_.pointer)
res45: Iterable[Sesame#Node] = List(
  "Axel Polleres"^^<http://www.w3.org/2001/XMLSchema#string>,
  "Christoph  Görn"^^<http://www.w3.org/2001/XMLSchema#string>,
...)
```

Above we have explored the data in one remote resource. But what about the documents that resource links to?

# Following links 

## Purpose and method

In the [Friend of a Friend](http://xmlns.com/foaf/spec/) profile we downloaded above, Henry keeps the names of the people he knows, so that 

 * if a link goes bad, he can remember whom he intended the link to refer to (in order to fix it if possible)
 * to allow user interfaces to immediately give some information about what he was intending to link to, without having to downloading more information.

But most of the information is actually not in his profile - why after all should he keep his profile
up to date about where his friends live, who their friends are, what their telephone number is, 
where their blogs are located, etc...? If he did not share responsibility with others in keeping
data up to date, he would soon have to maintain all the information in the world. 

That is where linked data comes in: it allows different people and organisations to share the burden of maintaining information. The URLs used in the names of the relations and the names
of the subjects and objects refer (directly and often indirectly via urls ending in #entities) to documents
maintained by others - in this case the people Henry knows. 

So the next step is to follow links from one resource to another, download those documents, turn them
into graphs, etc... We can do this if the pointers of the `PointedGraph` we named `knows` 
are urls that don't belong to the original  document (ie are not #urls that belong to that document, 
blank nodes or literals). Then for each such URL `url` having  downloaded the documents that those URLs point to, parsed them into a graph `g` and created a pointed graph `PointedGraph(url,g)` we can then continue
exploring the data from that location. 

We need to automate this process as much as possible though. In the above example we asked for the default representation of the `henryDocUrl` resource.
As it happened it returned Turtle, and we were then able to use the right parser. But we would like a library to automate this task so that the software can follow the `knows/foaf.knows` links to other pages automatically.

Let us write this then as little scripts and see how far we get.

## Fetching and parsing docs

So, first of all, we'd like to have one simple function that takes a URL and returns the pointed graph of that URL if successful or some explanation of what went wrong.

A little bit of playing around and we arrive at this function, that nicely
gives us all the information we need:

```Scala
@ def fetch(point: Sesame#URI):  HttpResponse[scala.util.Try[PointedGraph[Sesame]]] = {
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

1. As we follow links on the web we would like to tell the server we come across what types of mime types we understand so we increase the likelihood that it sends one we can parse. For Sesame [we currently can parse](https://github.com/banana-rdf/banana-rdf/blob/series/0.8.x/sesame/src/main/scala/org/w3/banana/sesame/SesameModule.scala) the following syntaxes for RDF: [RDF/XML](https://www.w3.org/TR/rdf-syntax-grammar/) popular in the early 2000s when XML was popular, [NTriples](https://www.w3.org/TR/n-triples/) the easiest to parse, [Turtle](https://www.w3.org/TR/turtle/) the easiest to read, [json-ld](https://json-ld.org/) popular because of it's encoding in JSON.

2. When we receive the response we need to select the parser given the mime type of the document returned by the server.

Here is a version that addresses both of these questions:


```Scala
import scala.util.{Try,Success,Failure}
def fetch(docUrl: Sesame#URI): HttpResponse[scala.util.Try[Sesame#Graph]] = {
  assert (docUrl == docUrl.fragmentLess)
  Http(docUrl.toString)
    .header("Accept", "application/rdf+xml,text/turtle,application/ld+json,text/n3;q=0.2")
    .exec { case (code, headers, is) =>
      headers.get("Content-Type")
             .flatMap(_.headOption)
             .fold[Try[(io.RDFReader[Sesame, Try, _],String)]](
                   Failure(new java.lang.Exception("Missing Content Type"))
              ) { ct =>
        val ctype = ct.split(';')
        val parser = ctype(0).trim match {
          case "text/turtle" => Success(turtleReader)
          case "application/rdf+xml" => Success(rdfXMLReader)
          case "application/n-triples" => Success(ntriplesReader)
          case "application/ld+json" => Success(jsonldReader)
          case ct => Failure(new java.lang.Exception("Missing parser for "+ct))
        }
        parser.map{ p => 
             val attributes = ctype.toList.tail.flatMap(
             _.split(',').map(_.split('=').toList.map(_.trim))
          ).map(avl => (avl.head, avl.tail.headOption.getOrElse(""))).toMap
          val encoding = attributes.get("encoding").getOrElse("utf-8")
          (p, encoding)
        }
       } flatMap { case (parser,encoding) =>  
        parser.read(new java.io.InputStreamReader(is, encoding), docUrl.toString)
      }
    }
}
```

The above functions show that dealing with the mime types is a little tricky perhaps, but not that difficult. The code was written entirely in the Ammonite shell (and that is perhaps the longest piece of code that makes sense to write there).

```Scala
@ val bblgrph = fetch(URI("http://bblfish.net/people/henry/card"))
bblgrph: HttpResponse[Try[org.openrdf.model.Model]] = HttpResponse(
  Success(
    [(http://axel.deri.ie/~axepol/foaf.rdf#me, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person) [null], 
     (http://axel.deri.ie/~axepol/foaf.rdf#me, http://xmlns.com/foaf/0.1/name, "Axel Polleres"^^<http://www.w3.org/2001/XMLSchema#string>) [null], ...]
```

# Efficiency improvements: Asynchrony and Caching

As we will want to fetch a number of graphs by following the `foaf:knows` links, we would like to do this in parallel. 

At this point, the [`java.net.HttpURLConnection`](https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html) starts showing its age and limitations as it is a blocking call that holds onto a thread. And threads are expensive: over half a MB each. This may not sound like a lot but if you want to open 1000 threads simultaneously you would end up using up half a Gigabyte just in thead overhead, and your system would become very slow as the Virtual Machine will keep jumping through 1000 threads just waiting to see if any one of them has something to parse, where most of the time they won't - as internet connections are close to 1 billion times slower than fetching information from an internal CPU cache.

But in order to avoid bringing in too many other concepts at this point let us deal with this the simple way, using threads and Futures. This will be good enough for a demo application and will provide a stepping stone to the more advanced tools. It will also make sure that you will notice if you start hammering the internet, as your computer will slow down quite quickly.

So, first of all, we need an execution context. We don't want to use the `scala.concurrent.ExecutionContext.global`
default one, as we may easily create a lot of threads. So we create our own. (Remember we can later take this code and turn it into a script that we can import in one go.)

```Scala
import scala.concurrent.{Future,ExecutionContext}
import java.util.concurrent.ThreadPoolExecutor
val threadPooleExec = new ThreadPoolExecutor(2,50,20,java.util.concurrent.TimeUnit.SECONDS,new java.util.concurrent.LinkedBlockingQueue[Runnable])
implicit lazy val webcontext = ExecutionContext.fromExecutor(threadPooleExec,err=>System.out.println("web error: "+err))
```

Next, we can create a very simple cache class. This one is not synchronised and should only be used by one thread.
It would be easy to create a more battle proof cache with [`java.util.concurrent.atomic.AtomicReference`](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicReference.html), but I'll leave that as an exercise to the reader. This makes the code easier to read.

```Scala
case class HttpException(docURL: Sesame#URI, code: Int, headers: Map[String, IndexedSeq[String]]) extends java.lang.Exception
case class ParseException(docURL: Sesame#URI, parseException: java.lang.Throwable) extends java.lang.Exception

class Cache(implicit val ex: ExecutionContext) {
   import scala.collection.mutable.Map
   val db = Map[Sesame#URI, Future[HttpResponse[Try[Sesame#Graph]]]]()
   def get(uri: Sesame#URI): Future[HttpResponse[Try[Sesame#Graph]]] = {
     val doc = uri.fragmentLess
     db.get(doc) match {
       case Some(f) => f
       case None => {
         val res = Future( fetch(doc) )
         db.put( doc, res )
         res
       }
     }
  }

  def getPointed(uri: Sesame#URI): Future[PointedGraphWeb] =
     get(uri).flatMap{ response =>
         if (! response.is2xx ) Future.failed(HttpException(uri, response.code, response.headers))
         else {
           val moreTryInfo = response.body.recoverWith{ case t => Failure(ParseException(uri,t)) }
           Future.fromTry(moreTryInfo).map{g=>
               PointedGraphWeb(uri.fragmentLess,PointedGraph[Sesame](uri,g),response.headers.toMap)
           }
        }
     }

  case class PointedGraphWeb(webDocURL: Sesame#URI,
                           pointedGraph: PointedGraph[Sesame],
                           headers: scala.collection.immutable.Map[String,IndexedSeq[String]]) {
        def jump(rel: Sesame#URI): Seq[Future[PointedGraphWeb]] =
          (pointedGraph/rel).toSeq.map{ pg =>
              if (pg.pointer.isURI) Cache.this.getPointed(pg.pointer.asInstanceOf[Sesame#URI])
              else Future.successful(this.copy(pointedGraph=PointedGraph[Sesame](pg.pointer, pointedGraph.graph)))
       }
    }
 }
```

Because dealing with types such as `Future[HttpResponse[Try[Sesame#Graph]]]` returned by the `Cache.get`
method, it is easier to compress that information into a PointedGraphWeb, which is a pointedGraph on the web. It contains the name of the graph, and some headers so that one can also work out the version and date of it.

Here is a slightly simplified picture of a PointedGraphWeb in the style of the previous ones:

![following the foaf:knows in timbl pointed graph](https://raw.githubusercontent.com/wiki/banana-rdf/banana-rdf/img/PointedGraphWeb.png)


```Scala
@ val cache = new Cache()
cache: Cache = ammonite.$sess.cmd162$Cache@64454830
@ val bblFuture = cache.getPointed(URI("http://bblfish.net/people/henry/card#me"))
res164: Future[HttpResponse[Try[org.openrdf.model.Model]]] = Future(<not completed>)
```

At this point, the `bblFuture` a Future of an HttpResponse is `<not completed>`. But if we wait just a little we get:

```Scala
@ bblFuture.isCompleted
res165: Boolean = true
```

Good! so we can use the above now to follow the links.
First let's unwrap the future to get at its PointedGraphWeb content, which
means digging through a few layers of this layer monad. (There are more elegant ways to do this)

```Scala
@ val pgw = bblFuture.value.get.get
pwg: cache.PointedGraphWeb = PointedGraphWeb(
  http://bblfish.net/people/henry/card,
  org.w3.banana.PointedGraph$$anon$1@34245b5d,
  Map(...))
```

Then we can use the `jump` to get all the other foaf files linked
to from bblfish's profile. 

```  
@ val bblFoafs = pgw.jump(foaf.knows)
res27: Seq[Future[cache.PointedGraphWeb]] = Stream(
  Future(<not completed>),
  Future(<not completed>),
  Future(<not completed>),
  ...)
```

Here is a picture that shows how the `jump` function (also written `~>`) 
differs from the `/` function we used previously.

![jumping the foaf:knows links](https://raw.githubusercontent.com/wiki/banana-rdf/banana-rdf/img/TheJumpFunction.png)

Notice that in the first result returned in the image the name of the graph and the graph are still the same. This is because Vint Cerf's is represented by a blank node, and not a URL so there is no place to jump. On the other hand the `bblfish.net` url, there is a place to jump. And that is indeed where we did jump: it is the graph that we have been looking at since the last section. But the result of our programmatically run `jump` across all of the other links comes to over 70 new links.

The previous diagram does not show the servers that are jumped across. As this is very important to understanding the difference between what the semantic web allows and normal siloed data strategies make possible, I have added this to the picture below. Note that we did not jump across just 2 servers, but tried to access something close to 70 different servers in our above jump call!

![jumping the foaf:knows links](https://raw.githubusercontent.com/wiki/banana-rdf/banana-rdf/img/jumpingAround.png)


If a little later we try again we will see that more and more of the futures are completed, we can the start looking at the results to see what the problems with the links may be:

```Scala
@ val finished = bblFoafs.map(_.value).filter(! _.isEmpty).toList.size
finished: Int = 74
@ val successful = bblFoafs.map(_.value).filter(optRes =>  !optRes.isEmpty && optRes.get.isSuccess)
successful: Seq[Option[Try[cache.PointedGraphWeb]]] = Stream(
  Some(
    Success(
      PointedGraphWeb(
        http://crschmidt.net/foaf.rdf,
        org.w3.banana.PointedGraph$$anon$1@6fd7afad,
     ...)))
@val failure = bblFoafs.map(_.value).filter(optRes =>  !optRes.isEmpty && optRes.get.isFailure)
failure: Seq[Option[Try[cache.PointedGraphWeb]]] = Stream(
  Some(
    Failure(
      HttpException(
        http://axel.deri.ie/~axepol/foaf.rdf#me,
        302,...))))
@ successful.size
res38: Int = 25
@ failure.size
res39: Int = 49
```

So it looks like we have quite a lot of failures. That may well
be. Henry has not had a tool to verify his foaf profile before,
so he has not been able to keep his profile fresh. 

With these tools we can see that we are well on our way to making 
this much easier, and well on our way to start automating it...


## Limitations of the above 

This document is about introducing people to banana-rdf with an example that makes it clear why this framework is useful. with data that spans organisations, just as html hypertext only makes sense if one publishes one's documents in a global space (otherwise word documents would have been fine).
This required going beyond what banana-rdf offers, by tying it into http requests etc... There is a lot more to be said about that.

Still, this little experiment has shown quite a few things that would need to be produced by a good web cache library:
  
 * it would need to limit requests to servers in an intelligent way in order to avoid flooding a server and getting the user banned from connecting. There are well-known rules for how crawlers should behave, and the code using the cache is getting close to being a crawler
 * it would be good if that web cache used as few threads as possible (eg. by using an actor framework, such as [akka](http://akka.io/) or akka-http)
 * the web cache should save the downloaded files to the local disk so that restart does not require fetching nonexpired files again
 
It would be nice to enhance the demonstration with the code the rest of the [solid](https://github.com/solid/solid) feature lists such as: 
 
 * Authentication with [WebID](http://webid.info/spec/)
 * Access Control 
 * Read-Write features given by LDP
 
IT may be that those make more sense in a different project, as they go too far from being directly relevant to banana-rdf. When such libraries do come to appear, it would be good if those projects continued the work here to show how to quickly get going with those libraries using Ammonite. 
 

# References

The concepts presented here in a practical way were part of a presentation at Scala eXchange given in 2014. This presentation and this hack session go well together - indeed I used the pictures from the presentation to illustrate the code here.

([slides here in pdf](http://bblfish.net/tmp/2014/12/08/SecureSocialWeb-Scala_eXchange.pdf))

[![skillsmatter video: building a secure social web using scala and scala-js](https://cloud.githubusercontent.com/assets/124506/5917678/facf06b0-a61f-11e4-97fd-2457f26a46b2.png)](https://skillsmatter.com/skillscasts/5960-building-a-secure-distributed-social-web-using-scala-scala-js)


# Todo

* allow the user to choose whether he wishes to use Sesame, Jena or Plantain with by genericizing the code to `Rdf` and allowing the user to choose whether
 `val Rdf = Sesame` or `val Rdf=Jena` ...
* Save the code to scripts and add them to banana-rdf repo so that one can tweak them more easily - to avoid the cut and paste required above 

