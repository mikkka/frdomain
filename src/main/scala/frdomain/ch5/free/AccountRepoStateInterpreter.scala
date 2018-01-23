package frdomain.ch5.free

import cats.data.StateT
import cats.instances.either._
import cats.{Monad, ~>}

object AccountRepoStateInterpreter {
  type Error = String
  type AccountMap = Map[String, Account]
  type Err[A] = Either[Error, A]
  type AccountState[A] = StateT[Err, AccountMap, A]
}

import AccountRepoStateInterpreter._

class AccountRepoStateInterpreter extends AccountRepoInterpreter[AccountState] {
  override implicit def ev: Monad[AccountState] = implicitly[Monad[AccountState]]

  override def step: ~>[AccountRepoF, AccountState] = new (AccountRepoF ~> AccountState) {
    override def apply[A](fa: AccountRepoF[A]): AccountState[A] = fa match {
      case Query(no) =>
        StateT {accs =>
          accs.get(no).map((accs, _)).toRight(s"Account no $no not found")
        }
      case Store(account) =>
        StateT.modify {accs =>
          accs + (account.no -> account)
        }
      case Delete(no) =>
        StateT.modify {accs =>
          accs - no
        }
    }
  }
}
