package frdomain.ch3.repository.reader

import scala.language.{higherKinds, implicitConversions}

object Syntax {
  implicit class FunctorSyntax[F[_]: Functor, A](a: F[A]) {
    def map[B](f: A => B) = Functor[F].map(a)(f)
  }

  implicit class Function1FunctorSyntax[A1, A](a: (A1) => A) {
    def map[B](f: A => B) = Functor[(A1) => ?].map(a)(f)
  }

  implicit class ReaderFunctorSyntax[A1, A](a: Reader[A1, A]) {
    def map[B](f: A => B) = Functor[Reader[A1, ?]].map(a)(f)
  }

  implicit class MonadSyntax[M[_]: Monad, A](a: M[A]) {
    def unit[A](a: => A) = Monad[M].unit(a)

    def flatMap[B](f: A => M[B]) = Monad[M].flatMap(a)(f)
  }

  implicit class Function1MonadSyntax[A1, A](a: (A1) => A) {
    def unit[A](a: => A) = Monad[(A1) => ?].unit(a)

    def flatMap[B](f: A => A1 => B) = Monad[(A1) => ?].flatMap(a)(f)
  }

  implicit class ReaderMonadSyntax[A1, A](a: Reader[A1, A]) {
    def unit[A](a: => A) = Monad[Reader[A1, ?]].unit(a)

    def flatMap[B](f: A => Reader[A1, B]) = Monad[Reader[A1, ?]].flatMap(a)(f)
  }
}
