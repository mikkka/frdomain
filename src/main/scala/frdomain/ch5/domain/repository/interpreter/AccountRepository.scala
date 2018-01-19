package frdomain.ch5
package domain
package repository
package interpreter

import java.util.Date

import cats.data.NonEmptyList
import cats.syntax.either._

import scala.collection.mutable.{Map => MMap}
import model.{Account, Balance}

trait AccountRepositoryInMemory extends AccountRepository {
  lazy val repo = MMap.empty[String, Account]

  def query(no: String): Either[NonEmptyList[String], Option[Account]] = repo.get(no).asRight
  def store(a: Account): Either[NonEmptyList[String], Account] = {
    val r = repo += ((a.no, a))
    a.asRight
  }
  def query(openedOn: Date): Either[NonEmptyList[String], Seq[Account]] = repo.values.filter(_.dateOfOpen == openedOn).toSeq.asRight
  def all: Either[NonEmptyList[String], Seq[Account]] = repo.values.toSeq.asRight
}

object AccountRepositoryInMemory extends AccountRepositoryInMemory