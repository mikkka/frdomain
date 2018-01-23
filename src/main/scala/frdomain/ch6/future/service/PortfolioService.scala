package frdomain.ch6
package future
package service

import java.util.Date
import cats.data.Kleisli

import scala.language.higherKinds
import repository.AccountRepository
import model._
import scala.concurrent._

trait PortfolioService {
  type PFOperation[A] = Kleisli[Future, AccountRepository, Seq[A]]

  def getCurrencyPortfolio(no: String, asOf: Date): PFOperation[Balance]
  def getEquityPortfolio(no: String, asOf: Date): PFOperation[Balance]
  def getFixedIncomePortfolio(no: String, asOf: Date): PFOperation[Balance]
}


