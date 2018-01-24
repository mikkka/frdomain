package frdomain.ch6
package task
package repository
package interpreter

import java.util.Date

import cats.data.NonEmptyList
import cats.syntax.either._

import scala.collection.mutable.{Map => MMap}

import model._

trait AccountRepositoryInMemory extends AccountRepository {
  lazy val repo = MMap.empty[String, Account]
  lazy val ccyBalanceRepo = MMap.empty[(String, Date), List[Balance]]
  lazy val equityBalanceRepo = MMap.empty[(String, Date), List[Balance]]
  lazy val fixedIncomeBalanceRepo = MMap.empty[(String, Date), List[Balance]]

  def query(no: String): Either[NonEmptyList[String], Option[Account]] = repo.get(no).asRight
  def store(a: Account): Either[NonEmptyList[String], Account] = {
    val r = repo += ((a.no, a))
    a.asRight
  }
  def query(openedOn: Date): Either[NonEmptyList[String], Seq[Account]] = repo.values.filter(_.dateOfOpen == openedOn).toSeq.asRight
  def all: Either[NonEmptyList[String], Seq[Account]] = repo.values.toSeq.asRight

  def getCurrencyBalance(no: String, asOf: Date): Either[String,List[Balance]] =
    ccyBalanceRepo.get((no, asOf)).getOrElse(List.empty[Balance]).asRight

  def getEquityBalance(no: String, asOf: Date): Either[String,List[Balance]] =
    equityBalanceRepo.get((no, asOf)).getOrElse(List.empty[Balance]).asRight

  def getFixedIncomeBalance(no: String, asOf: Date): Either[String,List[Balance]] =
    fixedIncomeBalanceRepo.get((no, asOf)).getOrElse(List.empty[Balance]).asRight
}

object AccountRepositoryInMemory extends AccountRepositoryInMemory

