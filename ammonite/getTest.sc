import $exec.akkaHttp
import run.cosy.auth.RSAKeys
import com.typesafe.sslconfig.akka._
import akka.actor._
import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory
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

//just to play with
val bblfish = AkkaUri("http://bblfish.net/people/henry/card#me")
val timbl = AkkaUri("https://www.w3.org/People/Berners-Lee/card#i")

val privStr = read(home/".keys"/"privateKey.pem")
val privKey = RSAKeys.readPrivateKeyFrom(privStr)
val localClient = run.cosy.auth.HttpSignature.Client(Uri("https://localhost:8443/2013/key#"),privKey.get)

val cosyClient = run.cosy.auth.HttpSignature.Client(Uri("https://cosy.run:8443/2013/key#"),privKey.get)
val web = new Web()
val fetch1 = web.GET(Web.rdfRequest(Uri("https://localhost:8443/2013/card")),4,List(),List(localClient))
val fetch2 = web.GET(Web.rdfRequest(Uri("https://cosy.run:8443/2013/card")),4,List(),List(cosyClient))

