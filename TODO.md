* pure Scala implementation for store (see with sandro to adapt definitions of g-*)
* revisite API/verbs: patch, append, etc.
* review the use of words like Syntax, Ops, etc.
* scalacheck for 2.9.1 is still used
* follow scalaz guidelines for organization Diesel and Banana
  * each functionality in its own trait
  * propose object that instantiate the trait
  * mix the trait in Banana[RDF] for bigger import (a la scalaz.Scalaz, but it's a typeclass)
  * current Diesel may be renamed, and Diesel may refer to the graph traversal and construction API -- we could call this big object org.w3.banana.Banana
* look at the immutable/mutable graph mess again
* make possible to instantiate Banana
* Reader: use Source
* Rdf#URI should be typed (what should I expect when I dereference?)
* Diesel must work on top of a Monad