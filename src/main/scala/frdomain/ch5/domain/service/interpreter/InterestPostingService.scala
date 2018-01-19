package frdomain.ch5
package domain
package service
package interpreter

import cats.data.Kleisli
import cats.data.NonEmptyList
import cats.syntax.either._

import model.{ Account, Balance }
import model.common._

class InterestPostingServiceInterpreter extends InterestPostingService[Account, Amount] {
  def computeInterest = Kleisli[Valid, Account, Amount] { (account: Account) =>
    if (account.dateOfClose isDefined) NonEmptyList.of(s"Account ${account.no} is closed").asLeft
    else Account.rate(account).map { r =>
      val a = account.balance.amount
      a + a * r
    }.getOrElse(BigDecimal(0)).asRight
  }

  def computeTax = Kleisli[Valid, Amount, Amount] { amount: Amount =>
    (amount * 0.1).asRight
  }
}

object InterestPostingService extends InterestPostingServiceInterpreter