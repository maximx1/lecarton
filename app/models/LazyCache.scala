package models

/**
 * Found at http://blog.adamklein.com/?p=689
 */

class LazyCache[T](v: => T) {
  private var state: Option[T] = None

  def value: T = if (state.isDefined) state.get else {
    state = Some(v)
    state.get
  }

  def reset(): Unit = {
    state = None
  }
}

object LazyCache {
  def apply[T](v: => T) = new LazyCache[T](v)
  implicit def unwrap[T](v: LazyCache[T]): T = v.value
}