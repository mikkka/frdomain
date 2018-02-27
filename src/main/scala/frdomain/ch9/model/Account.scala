package frdomain.ch9
package domain
package model

import java.util.{Calendar, Date}

import util.{Failure, Success, Try}
import cats.data.{NonEmptyList, ValidatedNel}
import cats.syntax.validated._
import cats.syntax.option._
import cats.syntax.apply._

object common {
  type Amount = BigDecimal

  def today = Calendar.getInstance.getTime
}

import common._

case class Balance(amount: Amount = 0)

sealed trait Account {
  def no: String
  def name: String
  def dateOfOpen: Option[Date]
  def dateOfClose: Option[Date]
  def balance: Balance
}

final case class CheckingAccount (no: String, name: String,
  dateOfOpen: Option[Date], dateOfClose: Option[Date] = None, balance: Balance = Balance()) extends Account

final case class SavingsAccount (no: String, name: String, rateOfInterest: Amount, 
  dateOfOpen: Option[Date], dateOfClose: Option[Date] = None, balance: Balance = Balance()) extends Account

object Account {

  case class AccountException(cause: Throwable) extends Throwable(cause)

  case class InvalidAccountNo(message: String) extends Throwable(message)
  case class InvalidOpenCloseDate(message: String) extends Throwable(message)
  case class InvalidInterestRate(message: String) extends Throwable(message)
  case class AlreadyClosed(message: String) extends Throwable(message)
  case class InsufficientBalance(message: String) extends Throwable(message)

  private def validateAccountNo(no: String): ValidatedNel[AccountException, String] =
    if (no.isEmpty || no.size < 5) 
      AccountException(InvalidAccountNo(s"Account No has to be at least 5 characters long: found $no")).invalidNel[String]
    else no.validNel[AccountException]

  private def validateOpenCloseDate(od: Date, cd: Option[Date]) = cd.map { c => 
    if (c before od) 
      AccountException(InvalidOpenCloseDate(s"Close date [$c] cannot be earlier than open date [$od]")).invalidNel[(Option[Date], Option[Date])]
    else (od.some, cd).validNel[AccountException]
  }.getOrElse { (od.some, cd).validNel[AccountException] }

  private def validateRate(rate: BigDecimal) =
    if (rate <= BigDecimal(0)) AccountException(InvalidInterestRate(s"Interest rate $rate must be > 0")).invalidNel[BigDecimal] else rate.validNel[AccountException]

  def checkingAccount(no: String, name: String, openDate: Option[Date], closeDate: Option[Date], 
    balance: Balance): Either[NonEmptyList[AccountException], Account] = {

    val od = openDate.getOrElse(today)

    (
      validateAccountNo(no),
      validateOpenCloseDate(openDate.getOrElse(today), closeDate)
    ).mapN { (n, d) =>
      CheckingAccount(n, name, d._1, d._2, balance)
    }.toEither
  }

  def savingsAccount(no: String, name: String, rate: BigDecimal, openDate: Option[Date], 
    closeDate: Option[Date], balance: Balance): Either[NonEmptyList[AccountException], Account] = {

    val od = openDate.getOrElse(today)

    (
      validateAccountNo(no),
      validateOpenCloseDate(openDate.getOrElse(today), closeDate),
      validateRate(rate)
    ).mapN { (n, d, r) =>
      SavingsAccount(n, name, r, d._1, d._2, balance)
    }.toEither
  }

  private def validateAccountAlreadyClosed(a: Account) = {
    if (a.dateOfClose isDefined) AccountException(AlreadyClosed(s"Account ${a.no} is already closed")).invalidNel[Account]
    else a.validNel[AccountException]
  }

  private def validateCloseDate(a: Account, cd: Date) = {
    if (cd before a.dateOfOpen.get) AccountException(InvalidOpenCloseDate(s"Close date [$cd] cannot be earlier than open date [${a.dateOfOpen.get}]")).invalidNel[Date]
    else cd.validNel[AccountException]
  }

  def close(a: Account, closeDate: Date): Either[NonEmptyList[AccountException], Account] = {
    (validateAccountAlreadyClosed(a), validateCloseDate(a, closeDate)).mapN { (acc, d) =>
      acc match {
        case c: CheckingAccount => c.copy(dateOfClose = Some(closeDate))
        case s: SavingsAccount  => s.copy(dateOfClose = Some(closeDate))
      }
    }.toEither
  }

  private def checkBalance(a: Account, amount: Amount) = {
    if (amount < 0 && a.balance.amount < -amount) AccountException(InsufficientBalance(s"Insufficient amount in ${a.no} to debit")).invalidNel[Account]
    else a.validNel[AccountException]
  }

  def updateBalance(a: Account, amount: Amount): Either[NonEmptyList[AccountException], Account] = {
    (validateAccountAlreadyClosed(a), checkBalance(a, amount)).mapN { (_, _) =>
      a match {
        case c: CheckingAccount => c.copy(balance = Balance(c.balance.amount + amount))
        case s: SavingsAccount  => s.copy(balance = Balance(s.balance.amount + amount))
      }
    }.toEither
  }

  def rate(a: Account) = a match {
    case SavingsAccount(_, _, r, _, _, _) => r.some
    case _ => None
  }
}



