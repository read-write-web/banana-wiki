***

## Running the server

In order to run a local server one must first locally clone the [Read-write-web play repository](https://github.com/read-write-web/rww-play). After that the user must navigate to their local Rww-play directory and run git there. For more information on how to run the server and the exact command used refer to the [Rww-play README page](https://github.com/read-write-web/rww-play).

Users can also publish on their local Read-write-web play. In order to do this, a user must run the following commands inside the local rww-play directory.

```bash
$ git 
> publishLocal
```

One can then import it into their scripts from inside the ammonite shell. The user should then be able to make a get on their local rww-play and check whether they can access a resource that has access control. This means that the user will need to make themselves a key.

## The Key Pair

A key pair is a pair formed from a Public and a Private key ----------- (more info on public and private keys ?)

### Resolving dependencies

In order to create this pair, the user must first import several dependencies from inside their ammonite shell.

```scala
@ import coursier.core.Authentication, coursier.MavenRepository

@ interp.repositories() ++= Seq(MavenRepository(
     "http://bblfish.net/work/repo/snapshots/"
  ))

@ import $ivy.`org.w3::banana-jena:0.8.5-SNAPSHOT`
@ import org.w3.banana._
@ import org.w3.banana.syntax._
@ import org.w3.banana.jena.Jena
@ import Jena._
@ import Jena.ops._
```

This will make sure that all of the Ivy dependencies are correctly resolved, getting information from the bblfish.net repository. After that the user must import the AkkaHttp signature support and the key functionality.

```scala
@ import $ivy.`run.cosy::akka-http-signature:0.2-SNAPSHOT`
@ import run.cosy.auth.RSAKeys
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

This command will generate a Public and Private key pair. As we can see only information about the Public key is being displayed in BigInt notation. The private key will be hidden from view - only the reference memory location will be shown.

From here many different representations can be shown of the modulus of the key.
One can show a String representation of the key modulus by using:
```scala
@ val stringPublic = pub.getModulus.toString

stringPublic: String = """Sun RSA public key, 2048 bits
  modulus: 18617616591683306896114159596818930151392321021655743092554571476906272216221226120621265951005438129779697999635487320523329319543618461573656972895570742121788481588357965539288547158041270238222755734178260848243117538931191184092136701393910768801017892322044665470799614169981926296276068464740129396666480935344897491410772746293264767223181696074228079861695236831569819678997680346803798366156295040872828579274684209593397084757356046266872087618604860475724560124477904326733099073845912710038077198861072318221134249054814455997792289530305256172821638522445655947916737229103835394051258962680413560258903
  public exponent: 65537"""

``` 

The keys' moduli can also be displayed in hexadecimal format:

```scala
@ val hexaPublic = pub.getModulus.toString(16)

hexaModul : String = "937adccd722bc982aed4847872b81e36b890bca13166714bc2befe4d8547b6218ecd2da1eb020198a4ea00e4db6757c7dda738ec8db8b3bf211d3a3a17e196a2035bc4c79d06d8a581487d9f49e86374712b10ef500dfa242a20cab52911e2636c9d99b21fe9768ef2381989a25dc8b0b7a46531249aac27c4b8ab451a19d5fbdfa5f78b0deac9778c7ff87cf6106ae4a6433466beb21df1265bf1fc9ab9cc80d7aff8cc4d0f67ae28647e4048da8df753493b8de6a8e0961416b4b37f7012907d2e756034b8a84c6e495c8f1d81f69843ae51379571d83b38e1c08a08c3748ef75ed7ecb016d0c8426b30c8c5060a08f87f6764b0ec14667cd6f0daa1244157"
```
The RSAKeys class' function .save() will return a String representation of the key in Base64 format

```scala
@ val basePublic = RSAKeys.save(pub)

basePublic: String = """MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk3rczXIryYKu1IR4crgeNriQvKExZnFL
wr7+TYVHtiGOzS2h6wIBmKTqAOTbZ1fH3ac47I24s78hHTo6F+GWogNbxMedBtilgUh9n0noY3Rx
KxDvUA36JCogyrUpEeJjbJ2Zsh/pdo7yOBmJol3IsLekZTEkmqwnxLirRRoZ1fvfpfeLDerJd4x/
+Hz2EGrkpkM0Zr6yHfEmW/H8mrnMgNev+MxND2euKGR+QEjajfdTSTuN5qjglhQWtLN/cBKQfS51
YDS4qExuSVyPHYH2mEOuUTeVcdg7OOHAigjDdI73XtfssBbQyEJrMMjFBgoI+H9nZLDsFGZ81vDa
oSRBVwIDAQAB"""
```

Keys can also be given different references: 

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
@ write(wd/"publicKey.pem", RSAKeys.save(pub))
@ write(wd/"privateKey.pem", RSAKeys.save(priv))
```
This will save the contents of the key in String format within the file found by following the specified path in .pem files.

### Transforming a Key into a Pointed Graph

In order to transform keys to pointed graphs one must first import multiple files in order to resolve the required dependencies.

The org.w3 declarations required are:

```scala
@ import org.w3.banana.binder
@ import org.w3.banana.binder.RecordBinder
@ implicit val binder = RecordBinder
@ val cert = CertPrefix[Rdf]
```
Several java imports from java's security and math library are also required for this process:

```scala
@ import java.math.BigInteger
@ import java.security.KeyFactory
@ import java.security.interfaces.RSAPublicKey
@ import java.security.spec.RSAPublicKeySpec
```

After that the binder and all it's functionalities can be imported:

```scala
@ import org.w3.banana.binder._
@ import recordBinder._
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
@ val keyGraph = pub.toPG

keyGraph: PointedGraph[Jena] = org.w3.banana.PointedGraph$$anon$1@7f8fa63d
```
One can then retrieve both the pointer and the graph:

```scala
@ keyJenGraph = keyGraph.graph

keyJenGraph: Jena#Graph =  {5880.026289.0-02638.0-6288.0-06411.0-23110.021178.029228.0 @http://www.w3.org/ns/auth/cert#exponent "65537"^^http://www.w3.org/2001/XMLSchema#integer; 5880.026289.0-02638.0-6288.0-06411.0-23110.021178.029228.0 @http://www.w3.org/ns/auth/cert#modulus "00937adccd722bc982aed4847872b81e36b890bca13166714bc2befe4d8547b6218ecd2da1eb020198a4ea00e4db6757c7dda738ec8db8b3bf211d3a3a17e196a2035bc4c79d06d8a581487d9f49e86374712b10ef500dfa242a20cab52911e2636c9d99b21fe9768ef2381989a25dc8b0b7a46531249aac27c4b8ab451a19d5fbdfa5f78b0deac9778c7ff87cf6106ae4a6433466beb21df1265bf1fc9ab9cc80d7aff8cc4d0f67ae28647e4048da8df753493b8de6a8e0961416b4b37f7012907d2e756034b8a84c6e495c8f1d81f69843ae51379571d83b38e1c08a08c3748ef75ed7ecb016d0c8426b30c8c5060a08f87f6764b0ec14667cd6f0daa1244157"^^http://www.w3.org/2001/XMLSchema#hexBinary}

@ keyJenPointed = keyGraph.pointer

keyJenPointed: Jena#Node = 5880.026289.0-02638.0-6288.0-06411.0-23110.021178.029228.0
```
The user can then choose to view this graph in a different format - like turtle for example. In order to do that however, more external libraries are required:

```scala
@ import org.w3.banana._
@ import org.w3.banana.syntax._
@ import ops._  
@ import org.w3.banana.jena.Jena
```
One can then represent the key's Pointed Graph in turtle: 

```scala
@ val toTurtle = turtleWriter.asString(keyGraph.graph,"").get

toTurtle: String = """<5880.026289.0-02638.0-6288.0-06411.0-23110.021178.029228.0>
        <http://www.w3.org/ns/auth/cert#exponent>
                65537 ;
        <http://www.w3.org/ns/auth/cert#modulus>
                "00937adccd722bc982aed4847872b81e36b890bca13166714bc2befe4d8547b6218ecd2da1eb020198a4ea00e4db6757c7dda738ec8db8b3bf211d3a3a17e196a2035bc4c79d06d8a581487d9f49e86374712b10ef500dfa242a20cab52911e2636c9d99b21fe9768ef2381989a25dc8b0b7a46531249aac27c4b8ab451a19d5fbdfa5f78b0deac9778c7ff87cf6106ae4a6433466beb21df1265bf1fc9ab9cc80d7aff8cc4d0f67ae28647e4048da8df753493b8de6a8e0961416b4b37f7012907d2e756034b8a84c6e495c8f1d81f69843ae51379571d83b38e1c08a08c3748ef75ed7ecb016d0c8426b30c8c5060a08f87f6764b0ec14667cd6f0daa1244157"^^<http://www.w3.org/2001/XMLSchema#hexBinary> .
"""
``` 
This will return a String representation of the Pointed Graph in turtle format. This representation can also be saved on your local file system like so:

```scala
@ write(wd/"publicKey.ttl", RSAKeys.save(toTurtle))
```
As is evident, the header is a link, which can be quite difficult to process. Because of this, the user can change it by using a file editor into something simpler - such as: *<#key>*

### Attaching Public keys to a File/URI

One can use the cp or the mv Ammonite commands to move the public key file into the test_www directory which resides within the rww-play directory. The process of attaching the file, containing the key to a URI is very similar.

```scala
@ cp(wd/"publicKey.ttl" , wd/"rww-play"/"test_www"/"pubKey.ttl")
```

The Akka-http-signature library makes use of symbolic links when accessing the public keys. One can make a symbolic link to their public key file using the following commands in the bash console:

```bash
$ ln -s pubKey.ttl pubKey
```
### Access Control

Currently in order to manipulate the access control one must manually edit the .acl.ttl file. To do this, one must navigate to the test_www directory (or the server that contains the Key files) and look for the correct file. After that in order to permit access to the , the user must comment out the following:

```bash
acl:agentClass <http://xmlns.com/foaf/0.1/Agent>;   =>    #  acl:agentClass <http://xmlns.com/foaf/0.1/Agent> ;
```
After that, the access to the Key files specified will be restricted. And therefore they will not be usable via the symbolic link created earlier. 

***
