package frdomain.ch8
package cqrs
package memrepo

import cats.Show
import org.joda.time.DateTime

object common {
  type Amount = BigDecimal
  type Error = String

  val today = DateTime.now()
}

import common._

case class Balance(amount: Amount = 0)

case class Account(no: String, name: String, dateOfOpening: DateTime = today, dateOfClosing: Option[DateTime] = None, 
  balance: Balance = Balance(0)) {
  def isClosed = dateOfClosing.isDefined
}

object Account {
  implicit val showAccount: Show[Account] = Show.show { case a: Account => a.toString }
}


