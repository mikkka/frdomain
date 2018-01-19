package frdomain.ch5
package free

import scala.collection.mutable.{Map => MMap}
import cats.effect.IO
import cats.~>
import cats.syntax.functor._
import cats.data.State

trait AccountRepoInterpreter {
  def apply[A](action: AccountRepo[A]): IO[A]
}
  
/**
 * Basic interpreter that uses a global mutable Map to store the state
 * of computation
 */
case class AccountRepoMutableInterpreter() extends AccountRepoInterpreter {
  val table: MMap[String, Account] = MMap.empty[String, Account]

  val step: AccountRepoF ~> IO = new (AccountRepoF ~> IO) {
    override def apply[A](fa: AccountRepoF[A]): IO[A] = fa match {
      case Query(no) =>
        table.get(no).map { a => IO(a) }
                     .getOrElse { IO.raiseError(new RuntimeException(s"Account no $no not found")) }

      case Store(account) => IO(table += ((account.no, account))).void
      case Delete(no) => IO(table -= no).void
    }
  }

  /**
   * Turns the AccountRepo script into a `Task` that executes it in a mutable setting
   */
  def apply[A](action: AccountRepo[A]): IO[A] = action.foldMap(step)
}

case class AccountRepoShowInterpreter() {

  type ListState[A] = State[List[String], A]

  val step: AccountRepoF ~> ListState = new (AccountRepoF ~> ListState) {
    private def show(s: String): ListState[Unit] =
      State(l => (l ++ List(s), ()))

    override def apply[A](fa: AccountRepoF[A]): ListState[A] = fa match {
      case Query(no) =>
        show(s"Query for $no").map(_ => Account(no, ""))
      case Store(account) =>
        show(s"Storing $account")
      case Delete(no) =>
        show(s"Deleting $no")
    }
  }

  def interpret[A](script: AccountRepo[A], ls: List[String]): List[String] =
    script.foldMap(step).run(ls).value._1

}
