

***

## Running the server

In order to run a local server one must first locally clone the [Read-write-web play repository](https://github.com/read-write-web/rww-play). After that the user must navigate to their local Rww-play directory and run sbt there. For more information on how to run the server and the exact command used refer to the [Rww-play README page](https://github.com/read-write-web/rww-play).

Users can also publish on their local Read-write-web play. In order to do this, a user must run the following commands inside the local rww-play directory.

```bash
$ sbt 
> publishLocal
```

One can then import it into their scripts from inside the ammonite shell. The user should then be able to make a GET request on their local rww-play and check whether they can access a resource that has access control. This means that the user will need to make themselves a key.

## The Key Pair

The Akka-Http-Signature library uses asymmetrical cryptography. As detailed in the [Public-Key cryptography wikipedia page](https://en.wikipedia.org/wiki/Public-key_cryptography), this is a cryptographic system that uses pairs of private and public keys. The library uses them to form Digital Signatures. In such a system public keys can be looked up by anyone and private keys are only known by their owner. Communication is achieved the following way : 
1. The sender sends a message with a header that is signed by his own private key 
2. The server then receives the message and uses the sender's public key to verify his identity

Because the messages have headers that are signed by the sender's private key and can only be verified via that sender's public key, and that public key is known to the server, the sender's identity can always be verified using it. This ensures a secure connection as all senders' identities can be verified when receiving messages. 

With the right signature, this cryptographic system can also ensure that the message is not changed in any way as the signature is bound to the original message and verification will fail if any alterations have occured. For more information on the matter, one can see the [Request for Comments Wiki page](https://en.wikipedia.org/wiki/Request_for_Comments)

For this to work, it must be easy for the user to generate a pair of a public and private key. The way this is done is by 
using the RSA algorithm to generate keys. These keys can be stored in your local file system or attached to a server. The private key should never be shared with anyone as it is the only way for the user to decrypt messages sent to him. This ensures access control is maintained and provides secure communication on the web

### Resolving dependencies

In order to create this pair, the user must first import several dependencies from inside their ammonite shell.

```scala
import coursier.core.Authentication, coursier.MavenRepository

interp.repositories() ++= Seq(MavenRepository(
     "http://bblfish.net/work/repo/snapshots/"
  ))

import $ivy.`org.w3::banana-jena:0.8.5-SNAPSHOT`
import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.jena.Jena
import Jena._
import Jena.ops._
```

This will make sure that all of the Ivy dependencies are correctly resolved, getting information from the bblfish.net repository. After that the user must import the AkkaHttp signature support and the key functionality.

```scala
import $ivy.`run.cosy::akka-http-signature:0.2-SNAPSHOT`
import run.cosy.auth.RSAKeys
``` 
After that, the user should be able to make a key pair using the imported libraries.

### Creating and using a key pair

A user can create a key pair using the commands found within the [Akka-http-signature Source code](https://github.com/fstoqnov/akka-http-signature/tree/master/src).

```scala
@ val (pub,priv) = RSAKeys.buildRSAKeyPair()

pub: java.security.interfaces.RSAPublicKey = Sun RSA public key, 2048 bits
  modulus: 18617616591683306896114159596818930151392321021655743092554571476906272216221226120621265951005438129779697999635487320523329319543618461573656972895570742121788481588357965539288547158041270238222755734178260848243117538931191184092136701393910768801017892322044665470799614169981926296276068464740129396666480935344897491410772746293264767223181696074228079861695236831569819678997680346803798366156295040872828579274684209593397084757356046266872087618604860475724560124477904326733099073845912710038077198861072318221134249054814455997792289530305256172821638522445655947916737229103835394051258962680413560258903
  public exponent: 65537
priv: java.security.interfaces.RSAPrivateKey = sun.security.rsa.RSAPrivateCrtKeyImpl@ffd26868
```

This command will generate a Public and Private key pair. As we can see only information about the Public key is being displayed in decimal notation. The private key will be hidden from view - only the reference memory location will be shown.

From here many different representations can be shown of the modulus of the key.
For example, the keys' moduli can also be displayed in hexadecimal format:

```scala
@ val hexaPublic = pub.getModulus.toString(16)

hexaPublic : String = "937adccd722bc982aed4847872b81e36b890bca13166714bc2befe4d8547b6218ecd2da1eb020198a4ea00e4db6757c7dda738ec8db8b3bf211d3a3a17e196a2035bc4c79d06d8a581487d9f49e86374712b10ef500dfa242a20cab52911e2636c9d99b21fe9768ef2381989a25dc8b0b7a46531249aac27c4b8ab451a19d5fbdfa5f78b0deac9778c7ff87cf6106ae4a6433466beb21df1265bf1fc9ab9cc80d7aff8cc4d0f67ae28647e4048da8df753493b8de6a8e0961416b4b37f7012907d2e756034b8a84c6e495c8f1d81f69843ae51379571d83b38e1c08a08c3748ef75ed7ecb016d0c8426b30c8c5060a08f87f6764b0ec14667cd6f0daa1244157"
```
The RSAKeys class' function .save() will return a String representation of the key in Base64 format. This mime type is based on the hexadecimal notation used in the PEM format readable by OpenSSL.

```scala
@ val basePublic = RSAKeys.save(pub)

basePublic: String = """MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk3rczXIryYKu1IR4crgeNriQvKExZnFL
wr7+TYVHtiGOzS2h6wIBmKTqAOTbZ1fH3ac47I24s78hHTo6F+GWogNbxMedBtilgUh9n0noY3Rx
KxDvUA36JCogyrUpEeJjbJ2Zsh/pdo7yOBmJol3IsLekZTEkmqwnxLirRRoZ1fvfpfeLDerJd4x/
+Hz2EGrkpkM0Zr6yHfEmW/H8mrnMgNev+MxND2euKGR+QEjajfdTSTuN5qjglhQWtLN/cBKQfS51
YDS4qExuSVyPHYH2mEOuUTeVcdg7OOHAigjDdI73XtfssBbQyEJrMMjFBgoI+H9nZLDsFGZ81vDa
oSRBVwIDAQAB"""
```

All keys can be converted from a PEM formatted String back to decimal format. The readPublicKeyFrom() function takes a String in Base64 notation and returns the original value of the key. This means that a public key, saved to a file in PEM format can always be converted back to it's original format by the user at a later point in time.

```scala
@ val publ = RSAKeys.readPublicKeyFrom(basePublic)

publ: scala.util.Try[java.security.PublicKey] = Success(
  Sun RSA public key, 2048 bits
  modulus: 18617616591683306896114159596818930151392321021655743092554571476906272216221226120621265951005438129779697999635487320523329319543618461573656972895570742121788481588357965539288547158041270238222755734178260848243117538931191184092136701393910768801017892322044665470799614169981926296276068464740129396666480935344897491410772746293264767223181696074228079861695236831569819678997680346803798366156295040872828579274684209593397084757356046266872087618604860475724560124477904326733099073845912710038077198861072318221134249054814455997792289530305256172821638522445655947916737229103835394051258962680413560258903
  public exponent: 65537
)
```
The .readPublicKeyFrom() function returns a Try of Public/Private key depending on whether the String given can be parsed back to some valid key. If the operation is successful the value of the original key is assigned to the new value in BigInt format.

The user can then save his keys on his local filesystem by using the following ammonite commands:

```scala
write(wd/"publicKey.pem", RSAKeys.save(pub))
write(wd/"privateKey.pem", RSAKeys.save(priv))
```
This will save the contents of the key in String format within the file found by following the specified path in .pem files.

### Saving a public Key to RDF

A user can also transform his public keys to an RDF. In order to do this one must first import multiple files in order to resolve the required dependencies.

The org.w3 declarations required are:

```scala
import org.w3.banana.binder
import org.w3.banana.binder.RecordBinder
implicit val binder = RecordBinder
val cert = CertPrefix[Rdf]
```
Several java imports from java's security and math library are also required for this process:

```scala
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
```

After that the binder and all it's functionalities can be imported:

```scala
import org.w3.banana.binder._
import recordBinder._
```

Finally, after that, the user can create the Cert object that contains the required dependencies to turn a Key into a Pointed Graph

```scala
@ object Cert {

   implicit val rsaClassUri = classUrisFor[RSAPublicKey](cert.RSAPublicKey)
   val factory = KeyFactory.getInstance("RSA")
   val exponent = property[BigInteger](cert.exponent)
   val modulus = property[Array[Byte]](cert.modulus)

   implicit val binder: PGBinder[Rdf, RSAPublicKey] =
   pgb[RSAPublicKey](modulus, exponent)(
      (m, e) => factory.generatePublic(new RSAPublicKeySpec(new BigInteger(m), e)).asInstanceOf[RSAPublicKey],
      key => Some((key.getModulus.toByteArray, key.getPublicExponent))
             ) // withClasses rsaClassUri
}

defined object Cert

@ import Cert._
```

After this all dependencies should be resolved and the user will be able to transform the keys into a Pointed Graph:

```scala
@ val keyPG = pub.toPG

keyPG : PointedGraph[RDF] = org.w3.banana.PointedGraph$$anon$1@7f8fa63d
```
One can then retrieve both the pointer and the graph:

```scala
@ val keyGraph = keyGraph.graph

keyGraph : RDF#Graph =  {5880.026289.0-02638.0-6288.0-06411.0-23110.021178.029228.0 http://www.w3.org/ns/auth/cert#exponent "65537"^^http://www.w3.org/2001/XMLSchema#integer; 5880.026289.0-02638.0-6288.0-06411.0-23110.021178.029228.0 @http://www.w3.org/ns/auth/cert#modulus "00937adccd722bc982aed4847872b81e36b890bca13166714bc2befe4d8547b6218ecd2da1eb020198a4ea00e4db6757c7dda738ec8db8b3bf211d3a3a17e196a2035bc4c79d06d8a581487d9f49e86374712b10ef500dfa242a20cab52911e2636c9d99b21fe9768ef2381989a25dc8b0b7a46531249aac27c4b8ab451a19d5fbdfa5f78b0deac9778c7ff87cf6106ae4a6433466beb21df1265bf1fc9ab9cc80d7aff8cc4d0f67ae28647e4048da8df753493b8de6a8e0961416b4b37f7012907d2e756034b8a84c6e495c8f1d81f69843ae51379571d83b38e1c08a08c3748ef75ed7ecb016d0c8426b30c8c5060a08f87f6764b0ec14667cd6f0daa1244157"^^http://www.w3.org/2001/XMLSchema#hexBinary}

@ val keyRDFPointer = keyGraph.pointer

keyRDFPointer: RDF#Node = 5880.026289.0-02638.0-6288.0-06411.0-23110.021178.029228.0
```
As is evident, the pointer is an automatically generated Blank Node, which can be quite difficult to process. Because of this, it would be more optimal to change the pointer to not be a blank node but rather a #uri. The user can change the pointer by using the following function : 

```scala
@ implicit class PGwrapper(val pg: PointedGraph[Rdf]) extends AnyVal {
  def rename(to: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] = {
    val oldNode = pg.pointer
    PointedGraph[Rdf](
      to,
      ops.makeGraph(pg.graph.triples.map{ triple =>
        ops.fromTriple(triple) match {
          case (oldNode,rel,obj) => ops.makeTriple(to,rel,obj)
          case (subj,rel,oldNode) => ops.makeTriple(subj,rel,to)
          case _ => triple
        }
      })
    )
  }
}

defined class PGwrapper
```

After this, one can simply change the header by into something simple by calling this function like so:

```scala
@ val finalKeyPG = keyPG.rename(URI("#key"))

finalKeyPG: PointedGraph[Rdf] = org.w3.banana.PointedGraph$$anon$1@4353a749

@ finalKeyPGPointer = finalKeyPG.pointer

finalKeyPGPointer : Rdf#Node = #key

@ finalKeyPGGraph = finalKeyPG.graph

finalKeyPGGraph: Rdf#Graph =  {#key @http://www.w3.org/ns/auth/cert#modulus "0087acb657366c6a911a0ce2470c24b8d85dd21ae76c6001db37d122c2eafe0fe2ae35541ccdd3c1c81603d98dd7d61b2c31c8605f81fbc3566604b7755793698836caaa99d868477c46b5b735529b50c0acfea6e10fc9b953697a67d57499801beb651e7e08343131a00d1873ab753ce5e79fc961874ee132472f7f210bc38966081fe263c620b67469b52cc555a26dee4fa10d8f40959e0e13516cc0bd1c1669ce53367d28248149142429b127f01e13d9bf21de52a4ac5694d5038a94178e144823b152fa19cbbc094dd40ddbd41e2195a19081125887fccedae21c50660c629a6f2ba4c85a54e1a6472f90b7b62ac7fe58fdae9daa0edc8edec49802fbfac7"^^http://www.w3.org/2001/XMLSchema#hexBinary; #key @http://www.w3.org/ns/auth/cert#exponent "65537"^^http://www.w3.org/2001/XMLSchema#integer}
```

As evident, the Blank node is now changed to something more readable and useful. 

The user can transform his key graph into a one of several well-known formats before publishing it on the server. One such format is turtle. In order to do that however, more external libraries are required:

```scala
import org.w3.banana._
import org.w3.banana.syntax._
import ops._  
import org.w3.banana.jena.Jena
```
One can then represent the rdf of the key in turtle format: 

```scala
@ val toTurtle = turtleWriter.asString(finalKeyPGGraph ,"").get

toTurtle: String = """<#key>  <http://www.w3.org/ns/auth/cert#exponent>
                65537 ;
        <http://www.w3.org/ns/auth/cert#modulus>
                "0087acb657366c6a911a0ce2470c24b8d85dd21ae76c6001db37d122c2eafe0fe2ae35541ccdd3c1c81603d98dd7d61b2c31c8605f81fbc3566604b7755793698836caaa99d868477c46b5b735529b50c0acfea6e10fc9b953697a67d57499801beb651e7e08343131a00d1873ab753ce5e79fc961874ee132472f7f210bc38966081fe263c620b67469b52cc555a26dee4fa10d8f40959e0e13516cc0bd1c1669ce53367d28248149142429b127f01e13d9bf21de52a4ac5694d5038a94178e144823b152fa19cbbc094dd40ddbd41e2195a19081125887fccedae21c50660c629a6f2ba4c85a54e1a6472f90b7b62ac7fe58fdae9daa0edc8edec49802fbfac7"^^<http://www.w3.org/2001/XMLSchema#hexBinary> .
"""
``` 
This will return a String representation of the Pointed Graph in turtle format. This representation can also be saved on your local file system like so:

```scala
write(wd/"publicKey.ttl", RSAKeys.save(toTurtle))
```

----------------------------

### Attaching Public keys to a File/URI

One can use the `cp` or the `mv` Ammonite commands to move the public key file into the test_www directory which resides within the rww-play directory. The process of attaching the file, containing the key to a URI is very similar.

```scala
cp(wd/"publicKey.ttl" , wd/"rww-play"/"test_www"/"pubKey.ttl")
```

The Rww-play web server, which the Akka-http-signature library makes use of, uses symbolic links to indicate default representations of different documents. The Http content negotiation functionality is thus preserved. Http Content negotiation is a mechanism that is used for representing different representations of a resource at the same URI. The user can then specify which is best suited for them. 

Content negotiation is preserved because the user can link to files without specifically addressing them via their original name by using these symbolic links. This makes it possible for links to be followed both on the file system and on the web in the same way. 

This can be quite a complex mechanism and the setup for it can vary greatly between different servers.

To create a symbolic link, one can make a symbolic link to their public key file using the following commands in the bash console:

```bash
$ ln -s pubKey.ttl pubKey
```
### Access Control

Currently in order to manipulate the access control one can use `curl` and `PATCH` commands [As detailed in the rww-play wiki page](https://github.com/read-write-web/rww-play/wiki/Curl-Interactions).

To demonstrate how access control changes user permitted actions, one can also manually edit the .acl.ttl file. To do this, one must navigate to the test_www directory (or the server that contains the Key files) and look for the correct file. After that in order to permit access to the , the user must comment out the `acl:agentClass <http://xmlns.com/foaf/0.1/Agent> ;`. This expression gives everyone permission to access this file and by commenting it out the user makes sure access is restricted only to them.

```bash
@prefix acl: <http://www.w3.org/ns/auth/acl#> . 
@prefix cert: <http://www.w3.org/ns/auth/cert#> .


[] acl:accessTo <pubKey>, <pubKey.ttl> ;

   acl:agent <pubKey#me> ;

#   acl:agent [ cert:key <publicKey#key> ];

#	acl:agentClass <http://xmlns.com/foaf/0.1/Agent> ;

	acl:mode acl:Read .

<> acl:include <.acl> .
```

This allows the user to manipulate access control by simply commenting out `acl:agent [ cert:key <publicKey#key> ];` 

After that, the access to the Key files specified will be restricted. And therefore they will not be usable via the symbolic link created earlier. 

If one now restarts the server and attempts to access the files, via curl for example, they will be presented with the following error message: 

```bash
$ curl -i -k https://localhost:8443/2013/pubKey

HTTP/1.1 404 Not Found
Content-Type: text/plain; charset=utf-8
Content-Length: 1093

could not find actor for Actor[akka://rww/user/rootContainer/card]rww.ldp.LDPExceptions$ResourceDoesNotExist: could not find actor for Actor[akka://rww/user/rootContainer/card]
        at rww.ldp.actor.RWWActorSystemImpl$$anonfun$3$$anon$1$$anonfun$receive$1.applyOrElse(RWWActorSystemImpl.scala:94)
        at akka.actor.Actor$class.aroundReceive(Actor.scala:467)
        at rww.ldp.actor.RWWActorSystemImpl$$anonfun$3$$anon$1.aroundReceive(RWWActorSystemImpl.scala:89)
        at akka.actor.ActorCell.receiveMessage(ActorCell.scala:516)
        at akka.actor.ActorCell.invoke(ActorCell.scala:487)
        at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:238)
        at akka.dispatch.Mailbox.run(Mailbox.scala:220)
        at akka.dispatch.ForkJoinExecutorConfigurator$AkkaForkJoinTask.exec(AbstractDispatcher.scala:397)
        at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
        at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
        at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
        at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
```
***
