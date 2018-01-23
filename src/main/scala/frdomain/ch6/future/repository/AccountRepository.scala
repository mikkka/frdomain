package frdomain.ch6
package future
package repository

import java.util.Date

import cats.data.NonEmptyList
import model._

trait AccountRepository { 
  def query(no: String): Either[NonEmptyList[String], Option[Account]]
  def store(a: Account): Either[NonEmptyList[String], Account]
  def query(openedOn: Date): Either[NonEmptyList[String], Seq[Account]]
  def all: Either[NonEmptyList[String], Seq[Account]]

  def getCurrencyBalance(no: String, asOf: Date): Either[String, Seq[Balance]]
  def getEquityBalance(no: String, asOf: Date): Either[String, Seq[Balance]]
  def getFixedIncomeBalance(no: String, asOf: Date): Either[String, Seq[Balance]]
}

