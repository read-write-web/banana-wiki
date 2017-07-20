
import coursier.core.Authentication, coursier.MavenRepository

interp.repositories() ++= Seq(MavenRepository(
  "http://bblfish.net/work/repo/snapshots/"
  ))

@

import $ivy.`org.w3::banana-jena:0.8.5-SNAPSHOT`
import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.jena.Jena
import Jena._
import Jena.ops._
