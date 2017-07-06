import scala.collection.{GenTraversableOnce, Seq, TraversableLike}
import scala.collection.generic.{GenericTraversableTemplate, SeqFactory, CanBuildFrom => CBF}
import scala.concurrent.{ExecutionContext, Future}

/**
  A few extension functions defined here to play around, that I will
  send to ammonite repo.
*/

/**
  * Extends collections to give short aliases for the commonly
  * used operations, so we can make it easy to use from the
  * command line.
  */
implicit class FilterMapFutureExt[+T](val future: Future[T]) extends AnyVal {
  /**
    * Alias for `map`
    */
  def |[B, That](f: T => B)(implicit ec: ExecutionContext) = future.map(f)

  /**
    * Alias for `flatMap`
    */
  def ||[B, That](f: T => Future[B])(implicit ec: ExecutionContext) = future.flatMap(f)

  /**
    * Alias for `foreach`
    */
  def |![B, That](f: T => Unit)(implicit ec: ExecutionContext) = future.foreach(f)

  /**
    * Alias for `filter`
    */
  def |?(p: T => Boolean)(implicit ec: ExecutionContext) = future.filter(p)

  /**
    * Alias for `collect`
    */
  def |?|[B, That](pf : scala.PartialFunction[T, B])(implicit ec: ExecutionContext) : Future[B] = future.collect(pf)

}

implicit class FilterMapExtPlus[+T, Repr](i: TraversableLike[T, Repr]) {
 /**
    * Alias for `collect`
    */
  def |?|[B, That](pf : scala.PartialFunction[T, B])(implicit bf : scala.collection.generic.CanBuildFrom[Repr, B, That]) : That = i.collect(pf)
}

