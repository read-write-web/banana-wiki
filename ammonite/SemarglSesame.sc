// import $ivy.`org.semarglproject:semargl-rdfa:0.7`
import $ivy.`org.semarglproject:semargl-sesame:0.7`
import scala.util.Try


//put this together for https://github.com/semarglproject/semargl/issues/50

implicit
class SesameParserExt(val parser: org.openrdf.rio.RDFParser) extends AnyVal {
  import java.io._

  def parseRdf(rdfa: String, base: String): org.openrdf.model.Model=
    parseRdf(new StringReader(rdfa),base)

  def parseRdf(is: InputStream, base: String): org.openrdf.model.Model =
    parseRdf(new InputStreamReader(is,"UTF-8"),base)


  def parseRdf(reader: Reader, base: String): org.openrdf.model.Model = {
    import org.openrdf.model.impl.{LinkedHashModel,ValueFactoryImpl}
    val model = new LinkedHashModel()
    val collector = new org.openrdf.rio.helpers.ContextStatementCollector(model,ValueFactoryImpl.getInstance())
    parser.setRDFHandler(collector)
    parser.parse(reader,base)
    model
  }
}

import org.semarglproject.sesame.rdf.rdfa.SesameRDFaParser
def RdfaParser(setup: SesameRDFaParser => Unit): SesameRDFaParser = {
   val p = new SesameRDFaParser()
   setup(p)
   p
}

import org.semarglproject.rdf.ProcessorGraphHandler
import org.openrdf.rio.RDFParser
import org.openrdf.rio.helpers.BasicParserSettings
import org.openrdf.rio.helpers.XMLParserSettings
import org.semarglproject.rdf.ParseException
import org.openrdf.rio.RDFParseException

case class TrackRDFParseException(val t: Throwable) extends RDFParseException(t)

class SemarglRdfXMLParser extends RDFParser with ProcessorGraphHandler {
  import org.semarglproject.source.StreamProcessor
  import org.semarglproject.rdf.RdfXmlParser
  import org.semarglproject.sesame.core.sink.SesameSink;
  import org.openrdf.rio.ParseErrorListener
  import java.io._
  import java.nio.charset.Charset
  import org.openrdf.rio.ParserConfig

  var parserConfig = new ParserConfig()
  var parseErrorListener: ParseErrorListener = null
  var preserveBNodeIDs = true

  val streamProcessor = {
        val sp = new StreamProcessor(RdfXmlParser.connect(SesameSink.connect(null)))
         sp.setProperty(StreamProcessor.PROCESSOR_GRAPH_HANDLER_PROPERTY, this)
         sp
    }


  def error(x$1: String,x$2: String): Unit = ???
  def info(x$1: String,x$2: String): Unit = ???
  def warning(x$1: String,x$2: String): Unit = ???

  // Members declared in org.openrdf.rio.RDFParser
  def getParserConfig(): org.openrdf.rio.ParserConfig = parserConfig
  def setParserConfig(config: org.openrdf.rio.ParserConfig): Unit =
    this.parserConfig = config

  def getRDFFormat(): org.openrdf.rio.RDFFormat =  org.openrdf.rio.RDFFormat.RDFXML

  def getSupportedSettings(): java.util.Collection[org.openrdf.rio.RioSetting[_]] = ???
  def parse(in: java.io.InputStream,base: String): Unit = {
    val reader = new InputStreamReader(in, Charset.forName("UTF-8"))
    try { parse(reader,base) } finally {
      try { reader.close() } catch { case e: IOException  =>  () }
    }
  }
  def parse(reader: java.io.Reader,base: String): Unit = {
    refreshSettings()
    try {
      streamProcessor.process(reader, base)
    } catch {
      case e: ParseException  => throw TrackRDFParseException(e);
    }
  }
  def setDatatypeHandling(x$1: org.openrdf.rio.RDFParser.DatatypeHandling): Unit = ???
  def setParseErrorListener(listener: org.openrdf.rio.ParseErrorListener): Unit = {
    parseErrorListener = listener
  }

  def setParseLocationListener(x$1: org.openrdf.rio.ParseLocationListener): Unit = ???

  def setVocabExpansionEnabled(vocabExpansionEnabled: Boolean): Unit = ???

  def setPreserveBNodeIDs(x$1: Boolean): Unit = ???
  def setRDFHandler(handler: org.openrdf.rio.RDFHandler): Unit = {
     streamProcessor.setProperty(SesameSink.RDF_HANDLER_PROPERTY, handler);
  }
  def setStopAtFirstError(stop: Boolean): Unit = ???
  def setValueFactory(valueFactory: org.openrdf.model.ValueFactory): Unit = {
    streamProcessor.setProperty(SesameSink.VALUE_FACTORY_PROPERTY, valueFactory)
  }
  def setVerifyData(x$1: Boolean): Unit = ???

  def refreshSettings(): Unit = {
    //todo: what should these be?
    //see https://github.com/semarglproject/semargl-sesame/blob/master/src/main/java/org/semarglproject/sesame/rdf/rdfa/SesameRDFaParser.java
    //eg:
    //streamProcessor.setProperty(RdfaParser.ENABLE_VOCAB_EXPANSION,
    //            parserConfig.get(RDFaParserSettings.VOCAB_EXPANSION_ENABLED))
  }
}

// see http://www.hars.de/2009/01/html-as-xml-in-scala.html
// object SemarglTest {
// import ammonite.ops._
//   val rdfa = read(pwd/"rdfa.info.txt")
//
//   import $ivy.`org.ccil.cowan.tagsoup:tagsoup:1.2.1`
//   val tagsoupParser =  org.ccil.cowan.tagsoup.jaxp.SAXParserImpl.newInstance(null)
//   println(Try{
//      RdfaParser(_.setXmlReader(tagsoupParser.getXMLReader())).parse(rdfa,"http://rdfa.info/")
//    })
//
//   import $ivy.`net.sourceforge.nekohtml:nekohtml:1.9.22`
//   val nekoParser = new org.cyberneko.html.parsers.SAXParser()
//   println(Try{
//     RdfaParser(_.setXmlReader(nekoParser)).parse(rdfa,"http://rdfa.info/")
//   })
//
//   Try(RdfaParser{p=>
//          p.setXmlReader(nekoParser);
//          p.setRdfaCompatibility(RDFaVersion.RDFA_1_1)
//     }.parse(rdfa,"http://rdfa.info/"))
// }
