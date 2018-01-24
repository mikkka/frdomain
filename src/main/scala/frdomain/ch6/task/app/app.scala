package frdomain.ch6
package task
package app

import cats.effect.IO
import cats.effect.IO._
import cats.syntax.apply._
import service.interpreter.PortfolioService
import repository.interpreter.AccountRepositoryInMemory
import model._
import common._

object Main {

  import PortfolioService._

  val accountNo = "a-123"
  val asOf = today

  val ccyPF: IO[List[Balance]] = getCurrencyPortfolio(accountNo, asOf)(AccountRepositoryInMemory)
  val eqtPF: IO[List[Balance]] = getEquityPortfolio(accountNo, asOf)(AccountRepositoryInMemory)
  val fixPF: IO[List[Balance]] = getFixedIncomePortfolio(accountNo, asOf)(AccountRepositoryInMemory)

  val r = (ccyPF, eqtPF, fixPF) mapN { (ccyP, eqtP, fixP) =>
    ccyP ++ eqtP ++ fixP
  }

  val portfolio = CustomerPortfolio(accountNo, asOf, r.unsafeRunSync())
}

