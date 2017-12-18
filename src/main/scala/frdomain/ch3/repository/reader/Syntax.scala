package frdomain.ch3.repository.reader

import scala.language.{higherKinds, implicitConversions}

object Syntax {
  implicit class FunctorSyntax[F[_]: Functor, A](a: F[A]) {
    def map[B](f: A => B) = Functor[F].map(a)(f)
  }

  implicit class Function1FunctorSyntax[A1, A](a: Function1[A1, A]) {
    def map[B](f: A => B) = Functor[({type f[x] = Function1[A1, x]})#f].map(a)(f)
  }

  implicit class ReaderFunctorSyntax[A1, A](a: Reader[A1, A]) {
    def map[B](f: A => B) = Functor[({type f[x] = Reader[A1, x]})#f].map(a)(f)
  }

  implicit class MonadSyntax[M[_]: Monad, A](a: M[A]) {
    def unit[A](a: => A) = Monad[M].unit(a)

    def flatMap[B](f: A => M[B]) = Monad[M].flatMap(a)(f)
  }

  implicit class Function1MonadSyntax[A1, A](a: Function1[A1, A]) {
    def unit[A](a: => A) = Monad[({type f[x] = Function1[A1, x]})#f].unit(a)

    def flatMap[B](f: A => A1 => B) = Monad[({type f[x] = Function1[A1, x]})#f].flatMap(a)(f)
  }

  implicit class ReaderMonadSyntax[A1, A](a: Reader[A1, A]) {
    def unit[A](a: => A) = Monad[({type f[x] = Reader[A1, x]})#f].unit(a)

    def flatMap[B](f: A => Reader[A1, B]) = Monad[({type f[x] = Reader[A1, x]})#f].flatMap(a)(f)
  }
}


