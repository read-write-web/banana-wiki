// make sure one of your scripts has already loaded this one somewhere
// todo: is there a way we can make sure this is done?
// import $exec.bblfishRepo

import $ivy.`run.cosy::solid-client:0.1-SNAPSHOT`

import run.cosy.auth.RSAKeys

import com.typesafe.sslconfig.akka._
import akka.actor._
import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory
import akka.event.Logging
import akka.stream.{ActorMaterializer, ActorMaterializerSettings,Supervision,_}
import akka.http.scaladsl.model.{Uri=>AkkaUri,_}

import scala.concurrent.ExecutionContext

import org.w3.banana.jena.Jena
import Jena._

//you need to place your files in ~/.keys
//see http://doc.akka.io/docs/akka/snapshot/scala/general/configuration.html
//and https://github.com/typesafehub/config
//and for the syntax see https://github.com/typesafehub/config/blob/master/HOCON.md

val shortScriptConf = ConfigFactory.parseString("""
   |akka {
   |#   loggers = ["akka.event.Logging$DefaultLogger"]
   |#   logging-filter = "akka.event.DefaultLoggingFilter"
   |#   loglevel = "ERROR"
   |
   |# see http://typesafehub.github.io/ssl-config/ExampleSSLConfig.html#id5
   |# and http://typesafehub.github.io/ssl-config/WSQuickStart.html#connecting-to-a-remote-server-over-https
   |   ssl-config {
   |#     loose.acceptAnyCertificate = true # <- only uncomment when completely at a loss about tls connections
   |      trustManager = {
   |        stores = [
   |          # 1. stores for test servers whose certificates can be retrieved with
   |          # keytool -printcert -sslserver localhost:8443 -rfc > ~/.keys/localhost_8443.crt
   |          { type = "PEM", path = ${user.home}/.keys/localhost_8443.crt }
   |          # 2. the default trust store
   |          { path : ${java.home}/lib/security/cacerts } # Fallback to default JSSE trust store
   |        ]
   |      } 
   |
   |      # for more info see: https://typesafehub.github.io/ssl-config/DebuggingSSL.html
   |      debug.ssl = false
   |   }
   |}
 """.stripMargin)
val regularConfig = ConfigFactory.load()
val ammConfig = shortScriptConf.withFallback(regularConfig).resolve
implicit val system = ActorSystem("akka_ammonite_script", ConfigFactory.load(ammConfig))

val log = Logging(system.eventStream, "banana-rdf")
implicit val materializer = ActorMaterializer(
                ActorMaterializerSettings(system).withSupervisionStrategy(Supervision.resumingDecider))
implicit val ec: ExecutionContext = system.dispatcher


object Test {
   import run.cosy.solid.client.Web._
   import java.security.interfaces.RSAPublicKey
   import run.cosy.auth.HttpSignature

   //just to play with
   lazy val bblfish = AkkaUri("http://bblfish.net/people/henry/card#me")
   lazy val timbl = AkkaUri("https://www.w3.org/People/Berners-Lee/card#i")
   
   lazy val privStr = read(home/".keys"/"privateKey.pem")
   lazy val pubStr = read(home/".keys"/"pubKey.pem")
   lazy val privKey = RSAKeys.readPrivateKeyFrom(privStr)
   lazy val pubKey = RSAKeys.readPublicKeyFrom(pubStr).map(_.asInstanceOf[RSAPublicKey])

   def pubKeyPG = {
    import ops._
    implicit val bind = run.cosy.crypto.Cert.binderWithName[Rdf](Uri("#key"))
    pubKey.map(_.toPG)
   } 

   lazy val localKey = run.cosy.auth.HttpSignature.Client(
        Uri("https://localhost:8443/2013/key#"),privKey.get)
  
   lazy val cosyKey = HttpSignature.Client(Uri("https://cosy.run:8443/2013/key#"),privKey.get)

   lazy val web = new run.cosy.solid.client.Web[Rdf]()

   import run.cosy.solid.RdfMediaTypes._
   import org.w3.banana.io.Turtle
   import web._
   def fetchLocal = web.run(GETrdf(Uri("https://localhost:8443/2013/card")),
                            keyChain = List(localKey))
   def fetchCosy = web.run(GETrdf(Uri("https://cosy.run:8443/2013/card")),
                           keyChain = List(cosyKey))

   def fetchLocalPg = {
        import Jena._
        web.GETpg(Uri("https://localhost:8443/2013/card"),List(localKey))
   }

   def postLocalWrongKey = web.run(
       req      = POST[`text/turtle`](
                    container = Uri("https://localhost:8443/2013/"),
                    graph     = pubKeyPG.get.graph,
                    slug      = Some("myKey")
                 ).get,
       keyChain = List(cosyKey)
   )
   def postLocalGoodKey = web.run(
       req     = POST[`text/turtle`](
                   container = Uri("https://localhost:8443/2013/"),
                   graph     = pubKeyPG.get.graph,
                   slug      = Some("myKey")
                 ).get,
       keyChain = List(localKey)
   )
 
   //it should be possible to see it first fail then succeed 
   def postLocalMultiKeys = web.run(
       req     = POST[`application/ld+json`](
                   container = Uri("https://localhost:8443/2013/"),
                   graph     = pubKeyPG.get.graph,
                   slug      = Some("myKey")
                 ).get,
       keyChain = List(cosyKey,localKey)
   )


}
