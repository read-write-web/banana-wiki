import $exec.akkaHttp
import run.cosy.auth.RSAKeys
import com.typesafe.sslconfig.akka._
import akka.actor._
import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory
//you need to set the rww-play environment
//see http://doc.akka.io/docs/akka/snapshot/scala/general/configuration.html
val shortScriptConf = ConfigFactory.parseString("""
   |rww.play=/Users/hjs/Programming/Scala/rww-play/dev  # put your dir here
   |akka {
   |#   loggers = ["akka.event.Logging$DefaultLogger"]
   |#   logging-filter = "akka.event.DefaultLoggingFilter"
   |#   loglevel = "ERROR"
   |
   |# see http://typesafehub.github.io/ssl-config/ExampleSSLConfig.html#id5
   |# and http://typesafehub.github.io/ssl-config/WSQuickStart.html#connecting-to-a-remote-server-over-https
   |   ssl-config {
   |#     loose.acceptAnyCertificate = true # <- only uncomment when debugging
   |      trustManager = {
   |        stores = [
   |          { type = "PEM", path = "/Users/hjs/Programming/Scala/rww-play/dev/conf/localhostCA.crt" }
   |          { path : ${java.home}/lib/security/cacerts } # Fallback to default JSSE trust store
   |        ]
   |      } 
   |   }
   |   ssl-config.debug = {
   |      ssl = true
   | #     trustmanager = true
   | #     keymanager = true
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

val privStr = read(up/'keys/"privateKey.pem")
val privKey = RSAKeys.readPrivateKeyFrom(privStr)
val localClient = run.cosy.auth.HttpSignature.Client(Uri("https://localhost:8443/2013/key#"),privKey.get)

val cosyClient = run.cosy.auth.HttpSignature.Client(Uri("https://cosy.run:8443/2013/key#"),privKey.get)
val web = new Web()
val fetch1 = web.GET(Web.rdfRequest(Uri("https://localhost:8443/2013/card")),4,List(),List(localClient))
val fetch2 = web.GET(Web.rdfRequest(Uri("https://cosy.run:8443/2013/card")),4,List(),List(cosyClient))

