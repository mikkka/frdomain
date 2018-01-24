package frdomain.ch6
package domain
package repository

import java.util.Date

import cats.data.NonEmptyList
import cats.syntax.either._

import model.{Account, Balance}

trait AccountRepository { 
  def query(no: String): Either[NonEmptyList[String], Option[Account]]
  def store(a: Account): Either[NonEmptyList[String], Account]
  def balance(no: String): Either[NonEmptyList[String], Balance] = query(no) match {
    case Right(Some(a)) => a.balance.asRight
    case Right(None) => NonEmptyList.of(s"No account exists with no $no").asLeft
    case Left(a) => a.asLeft
  }
  def query(openedOn: Date): Either[NonEmptyList[String], Seq[Account]]
  def all: Either[NonEmptyList[String], Seq[Account]]
}

