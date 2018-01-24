package frdomain.ch6
package domain
package model

import java.util.{ Date, Calendar }
import util.{ Try, Success, Failure }

import cats.data.NonEmptyList
import cats.syntax.validated._
import cats.syntax.option._
import cats.syntax.either._
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
  private def validateAccountNo(no: String) = 
    if (no.isEmpty || no.size < 5) s"Account No has to be at least 5 characters long: found $no".invalidNel[String]
    else no.validNel[String]

  private def validateOpenCloseDate(od: Date, cd: Option[Date]) = cd.map { c => 
    if (c before od) s"Close date [$c] cannot be earlier than open date [$od]".invalidNel[(Option[Date], Option[Date])]
    else (od.some, cd).validNel[String]
  }.getOrElse { (od.some, cd).validNel[String] }

  private def validateRate(rate: BigDecimal) =
    if (rate <= BigDecimal(0)) s"Interest rate $rate must be > 0".invalidNel[BigDecimal]
    else rate.validNel[String]

  def checkingAccount(no: String, name: String, openDate: Option[Date], closeDate: Option[Date], 
    balance: Balance): Either[NonEmptyList[String], Account] = {

    val od = openDate.getOrElse(today)

    (
      validateAccountNo(no),
      validateOpenCloseDate(openDate.getOrElse(today), closeDate)
    ).mapN { (n, d) =>
      CheckingAccount(n, name, d._1, d._2, balance)
    }.toEither
  }

  def savingsAccount(no: String, name: String, rate: BigDecimal, openDate: Option[Date], 
    closeDate: Option[Date], balance: Balance): Either[NonEmptyList[String], Account] = {

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
    if (a.dateOfClose isDefined) s"Account ${a.no} is already closed".invalidNel[Account]
    else a.validNel[String]
  }

  private def validateCloseDate(a: Account, cd: Date) = {
    if (cd before a.dateOfOpen.get) s"Close date [$cd] cannot be earlier than open date [${a.dateOfOpen.get}]".invalidNel[Date]
    else cd.validNel[String]
  }

  def close(a: Account, closeDate: Date): Either[NonEmptyList[String], Account] = {
    (validateAccountAlreadyClosed(a), validateCloseDate(a, closeDate)).mapN { (acc, d) =>
      acc match {
        case c: CheckingAccount => c.copy(dateOfClose = Some(closeDate))
        case s: SavingsAccount  => s.copy(dateOfClose = Some(closeDate))
      }
    }.toEither
  }

  private def checkBalance(a: Account, amount: Amount) = {
    if (amount < 0 && a.balance.amount < -amount) s"Insufficient amount in ${a.no} to debit".invalidNel[Account]
    else a.validNel[String]
  }

  def updateBalance(a: Account, amount: Amount): Either[NonEmptyList[String], Account] = {
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


