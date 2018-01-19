package frdomain.ch5
package domain
package service
package interpreter

import java.util.{Calendar, Date}

import cats.data.{Kleisli, NonEmptyList}
import cats.syntax.either._

import model.{Account, Balance}
import model.common._
import repository.AccountRepository

class AccountServiceInterpreter extends AccountService[Account, Amount, Balance] {

  def open(no: String, 
           name: String, 
           rate: Option[BigDecimal],
           openingDate: Option[Date],
           accountType: AccountType) = Kleisli[Valid, AccountRepository, Account] { (repo: AccountRepository) =>

    repo.query(no) match {
      case Right(Some(a)) => NonEmptyList.of(s"Already existing account with no $no").asLeft[Account]
      case Right(None)    => accountType match {
        case Checking =>
          Account.checkingAccount(no, name, openingDate, None, Balance()).flatMap(repo.store)
        case Savings  =>
          rate map { r =>
            Account.savingsAccount(no, name, r, openingDate, None, Balance()).flatMap(repo.store)
          } getOrElse {
            NonEmptyList.of(s"Rate needs to be given for savings account").asLeft[Account]
          }
      }
      case Left(a) => a.asLeft
    }
  }

  def close(no: String, closeDate: Option[Date]) = Kleisli[Valid, AccountRepository, Account] { (repo: AccountRepository) =>
    repo.query(no) match {
      case Right(None) => NonEmptyList.of(s"Account $no does not exist").asLeft[Account]
      case Right(Some(a)) =>
        val cd = closeDate.getOrElse(today)
        Account.close(a, cd).flatMap(repo.store)
      case Left(a) => a.asLeft
    }
  }

  def debit(no: String, amount: Amount) = up(no, amount, D)
  def credit(no: String, amount: Amount) = up(no, amount, C)

  private trait DC
  private case object D extends DC
  private case object C extends DC

  private def up(no: String, amount: Amount, dc: DC): AccountOperation[Account] = Kleisli[Valid, AccountRepository, Account] { (repo: AccountRepository) =>
    repo.query(no) match {
      case Right(None) => NonEmptyList.of(s"Account $no does not exist").asLeft[Account]
      case Right(Some(a)) => dc match {
        case D => Account.updateBalance(a, -amount).flatMap(repo.store) 
        case C => Account.updateBalance(a, amount).flatMap(repo.store) 
      }
      case Left(a) => a.asLeft
    }
  }

  def balance(no: String) = Kleisli[Valid, AccountRepository, Balance] { (repo: AccountRepository) => repo.balance(no) }
}

object AccountService extends AccountServiceInterpreter