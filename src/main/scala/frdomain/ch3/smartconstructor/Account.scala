package frdomain.ch3
package smartconstructor

import java.util.{Calendar, Date}

import frdomain.ch3.lens.Lens

import util.{Failure, Success, Try}

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

/**
 * In order to ensure that the user cannot use `apply` and `copy` as well, we need
 * to delegate both of them to the smart constructor. Still there can be some hairy issues
 * as in http://stackoverflow.com/questions/19462598/scala-case-class-implementation-of-smart-constructors
 **/
final case class CheckingAccount private (no: String, name: String,
  dateOfOpen: Option[Date], dateOfClose: Option[Date] = None, balance: Balance = Balance()) extends Account {

  def copy(no: String = no, 
    name: String = name, 
    dateOfOpen: Option[Date] = dateOfOpen, 
    dateOfClose: Option[Date] = dateOfClose, 
    balance: Balance = balance) = Account.checkingAccount(no, name, dateOfOpen, dateOfClose, balance)

  def apply(no: String = no, 
    name: String = name, 
    dateOfOpen: Option[Date] = dateOfOpen, 
    dateOfClose: Option[Date] = dateOfClose, 
    balance: Balance = balance) = Account.checkingAccount(no, name, dateOfOpen, dateOfClose, balance)
}

final case class SavingsAccount private (no: String, name: String, rateOfInterest: Amount, 
  dateOfOpen: Option[Date], dateOfClose: Option[Date] = None, balance: Balance = Balance()) extends Account {

  def copy(no: String = no, 
    name: String = name, 
    rateOfInterest: Amount = rateOfInterest,
    dateOfOpen: Option[Date] = dateOfOpen, 
    dateOfClose: Option[Date] = dateOfClose, 
    balance: Balance = balance) = Account.savingsAccount(no, name, rateOfInterest, dateOfOpen, dateOfClose, balance)

  def apply(no: String = no, 
    name: String = name, 
    rateOfInterest: Amount = rateOfInterest,
    dateOfOpen: Option[Date] = dateOfOpen, 
    dateOfClose: Option[Date] = dateOfClose, 
    balance: Balance = balance) = Account.savingsAccount(no, name, rateOfInterest, dateOfOpen, dateOfClose, balance)
}

object Account {
  def checkingAccount(no: String, name: String, openDate: Option[Date], closeDate: Option[Date], 
    balance: Balance): Try[CheckingAccount] = {

    closeDateCheck(openDate, closeDate).map { d =>
      CheckingAccount(no, name, Some(d._1), d._2, balance)
    }
  }

  def savingsAccount(no: String, name: String, rate: BigDecimal, openDate: Option[Date], 
    closeDate: Option[Date], balance: Balance): Try[SavingsAccount] = {

    println("in smart")
    closeDateCheck(openDate, closeDate).map { d =>
      if (rate <= BigDecimal(0)) 
        throw new Exception(s"Interest rate $rate must be > 0")
      else
        SavingsAccount(no, name, rate, Some(d._1), d._2, balance)
    }
  }

  private def closeDateCheck(openDate: Option[Date], closeDate: Option[Date]): Try[(Date, Option[Date])] = {
    val od = openDate.getOrElse(today)

    closeDate.map { cd =>
      if (cd before od) Failure(new Exception(s"Close date [$cd] cannot be earlier than open date [$od]")) 
      else Success((od, Some(cd)))
    }.getOrElse {
      Success((od, closeDate))
    }
  }
}


case class Lens[O, V](
                       get: O => V,
                       set: (O, V) => Try[O]
                     )

object Lens {
  def compose[Outer, Inner, Value](
                                    outer: Lens[Outer, Inner],
                                    inner: Lens[Inner, Value]
                                  ) = Lens[Outer, Value](
    get = outer.get andThen inner.get,
    set = (obj, value) => inner.set(outer.get(obj), value).flatMap(outer.set(obj, _))
  )
}

object AccountLenses {
  val noLensCA = Lens[CheckingAccount, String](
    get = _.no,
    set = (o, v) => o.copy(no = v)
  )

  val noLensSA = Lens[SavingsAccount, String](
    get = _.no,
    set = (o, v) => o.copy(no = v)
  )
}