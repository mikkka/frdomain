package frdomain.ch6
package domain
package service
package interpreter


import cats.data.{EitherT, Kleisli, NonEmptyList}
import cats.syntax.either._

import scala.concurrent._
import ExecutionContext.Implicits.global
import model.{Account, Balance}
import model.common._

class InterestPostingServiceInterpreter extends InterestPostingService[Account, Amount] {
  def computeInterest = Kleisli[Valid, Account, Amount] { (account: Account) =>
    EitherT {
      Future {
        if (account.dateOfClose isDefined) NonEmptyList.of(s"Account ${account.no} is closed").asLeft
        else Account.rate(account).map { r =>
          val a = account.balance.amount
          a + a * r
        }.getOrElse(BigDecimal(0)).asRight
      }
    }
  }

  def computeTax = Kleisli[Valid, Amount, Amount] { amount: Amount =>
    EitherT {
      Future {
        (amount * 0.1).asRight
      }
    }
  }
}

object InterestPostingService extends InterestPostingServiceInterpreter

