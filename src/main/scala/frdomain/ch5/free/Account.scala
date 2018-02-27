package frdomain.ch5
package free


import java.util.{Calendar, Date}

import cats.Show

object common {
  type Amount = BigDecimal

  val today = Calendar.getInstance.getTime
}

import common._

case class Balance(amount: Amount = 0)

case class Account(no: String, name: String, dateOfOpening: Date = today, dateOfClosing: Option[Date] = None, 
  balance: Balance = Balance(0))

object Account {
  implicit val showAccount: Show[Account] = Show.apply { case a: Account => a.toString }
}


