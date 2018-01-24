package frdomain.ch6
package task
package service
package interpreter

import java.util.{ Date, Calendar }

import cats.data.Kleisli
import cats.effect.IO

import model._
import model.common._
import repository.AccountRepository

class PortfolioServiceInterpreter extends PortfolioService {
  def getCurrencyPortfolio(no: String, asOf: Date) = Kleisli[IO, AccountRepository, List[Balance]] { (repo: AccountRepository) =>

    IO {
      repo.getCurrencyBalance(no, asOf) match {
        case Right(b) => b
        case Left(_) => throw new Exception(s"Failed to fetch currency balance")
      }
    }
  }

  def getEquityPortfolio(no: String, asOf: Date) = Kleisli[IO, AccountRepository, List[Balance]] { (repo: AccountRepository) =>

    IO {
      repo.getEquityBalance(no, asOf) match {
        case Right(b) => b
        case Left(_) => throw new Exception(s"Failed to fetch equity balance")
      }
    }
  }

  def getFixedIncomePortfolio(no: String,asOf: Date) = Kleisli[IO, AccountRepository, List[Balance]] { (repo: AccountRepository) =>

    IO {
      repo.getFixedIncomeBalance(no, asOf) match {
        case Right(b) => b
        case Left(_) => throw new Exception(s"Failed to fetch fixed income balance")
      }
    }
  }
}

object PortfolioService extends PortfolioServiceInterpreter