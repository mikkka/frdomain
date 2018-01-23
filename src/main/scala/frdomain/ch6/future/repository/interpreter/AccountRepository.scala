package frdomain.ch6
package future
package repository
package interpreter

import java.util.Date
import scala.collection.mutable.{ Map => MMap }
import cats.data.NonEmptyList
import cats.syntax.either._
import model._

trait AccountRepositoryInMemory extends AccountRepository {
  lazy val repo = MMap.empty[String, Account]
  lazy val ccyBalanceRepo = MMap.empty[(String, Date), Seq[Balance]]
  lazy val equityBalanceRepo = MMap.empty[(String, Date), Seq[Balance]]
  lazy val fixedIncomeBalanceRepo = MMap.empty[(String, Date), Seq[Balance]]

  def query(no: String): Either[NonEmptyList[String], Option[Account]] = repo.get(no).asRight
  def store(a: Account): Either[NonEmptyList[String], Account] = {
    val r = repo += ((a.no, a))
    a.asRight
  }
  def query(openedOn: Date): Either[NonEmptyList[String], Seq[Account]] = repo.values.filter(_.dateOfOpen == openedOn).toSeq.asRight
  def all: Either[NonEmptyList[String], Seq[Account]] = repo.values.toSeq.asRight

  def getCurrencyBalance(no: String, asOf: Date): Either[String, Seq[Balance]] =
    ccyBalanceRepo.get((no, asOf)).getOrElse(Seq.empty[Balance]).asRight

  def getEquityBalance(no: String, asOf: Date): Either[String, Seq[Balance]] =
    equityBalanceRepo.get((no, asOf)).getOrElse(Seq.empty[Balance]).asRight

  def getFixedIncomeBalance(no: String, asOf: Date): Either[String, Seq[Balance]] =
    fixedIncomeBalanceRepo.get((no, asOf)).getOrElse(Seq.empty[Balance]).asRight
}

object AccountRepositoryInMemory extends AccountRepositoryInMemory

