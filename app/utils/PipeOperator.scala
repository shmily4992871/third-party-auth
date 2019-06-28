package utils

import com.twitter.inject.Logging

object PipeOperator extends Logging {
  implicit class Pipe[T](val v: T) extends AnyVal {
    def |>[U](f: T => U): U = f(v)

    // Additional suggestions:
    def $$[U](f: T => U): T = {
      f(v); v
    }

    def #?(str: String = ""): T = {
      debug(s"$str: $v"); v
    }

    def #!(str: String = ""): T = {
      error(s"$str: $v"); v
    }

    def #|(str: String = ""): T = {
      info(s"$str: $v"); v
    }

  }

  implicit class BooleanPipe(val v: Boolean) extends AnyVal {
    def option[U](f: => U): Option[U] = if (v) Option(f) else None
  }

}
