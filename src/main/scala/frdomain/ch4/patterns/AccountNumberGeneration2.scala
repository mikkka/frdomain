package frdomain.ch4.patterns

import frdomain.ch3.repository.AccountRepository

import scala.util.Success

object AccountNumberGeneration2 {
  import cats.Monad
  import cats.data.State
  import cats.syntax.monad._

  final class Generator(rep: AccountRepository) {
    val no: String = scala.util.Random.nextString(10)

    def exists: Boolean = rep.query(no) match {
      case Success(Some(a)) => true
      case _ => false
    }
  }

  def generate(start: Generator, r: AccountRepository): Generator = {
    val mInsState = Monad[State[Generator, ?]]

    val StateGen = State[Generator, String] { init =>
      (init, init.no)
    }

    import StateGen._
    mInsState.whileM_(inspect(_.exists))(modify(_ => new Generator(r)))
      .run(start).value._1
  }
}
