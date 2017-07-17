// if one imports this more than once things import $file get problematic
// import coursier.core.Authentication, coursier.MavenRepository
//
// interp.repositories() ++= Seq(MavenRepository(
//   "http://bblfish.net/work/repo/releases/"
//   ))
//
// @

import $ivy.`org.w3::banana-sesame:0.8.4`
import $ivy.`net.sourceforge.nekohtml:nekohtml:1.9.22`


import java.io._
import org.w3.banana._
import org.w3.banana.io._
import org.w3.banana.jena.Jena

import scala.util.Try
import $file.SemarglSesame, SemarglSesame._
import org.openrdf.rio.helpers.RDFaVersion
import org.semarglproject.sesame.rdf.rdfa._


class SesameRDFaReader(implicit val ops: RDFOps[Sesame]) extends RDFReader[Sesame, Try, RDFaXHTML] {
  val nekoParser = new org.cyberneko.html.parsers.SAXParser()
  def read(in: InputStream, base: String): Try[Sesame#Graph] = {
    Try(RdfaParser{p=>
           p.setXmlReader(nekoParser);
           p.setRdfaCompatibility(RDFaVersion.RDFA_1_1)
      }.parseRdf(in,base))
  }

 /** Tries parsing an RDF Graph from a [[java.io.Reader]] and a base URI.
   * @param base the base URI to use, to resolve relative URLs found in the InputStream
   **/
 def read(reader: Reader, base: String): Try[Sesame#Graph] = {
   Try(RdfaParser{p=>
          p.setXmlReader(nekoParser);
          p.setRdfaCompatibility(RDFaVersion.RDFA_1_1)
     }.parseRdf(reader,base))
 }

}

class SesameRDFXMLReader(implicit val ops: RDFOps[Sesame]) extends RDFReader[Sesame, Try, RDFXML] {
  import  org.openrdf.rio._

  def read(in: InputStream, base: String): Try[Sesame#Graph] = {
    val xmlp = Rio.createParser(RDFFormat.RDFXML)
    Try(xmlp.parseRdf(in,base))
  }

 /** Tries parsing an RDF Graph from a [[java.io.Reader]] and a base URI.
   * @param base the base URI to use, to resolve relative URLs found in the InputStream
   **/
 def read(reader: Reader, base: String): Try[Sesame#Graph] = {
   val xmlp = Rio.createParser(RDFFormat.RDFXML)
   Try(xmlp.parseRdf(reader,base))
 }

}

// class SesameRDFXMLReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameReader[RDFXML] {
//   def getParser() = {
//     new org.semarglproject.rdf.RdfXmlParser()
//   }
// }
