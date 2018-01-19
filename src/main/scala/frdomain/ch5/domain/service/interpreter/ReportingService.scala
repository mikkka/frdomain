package frdomain.ch5
package domain
package service
package interpreter

import cats.data.Kleisli
import cats.syntax.either._
import repository.AccountRepository
import model.common._


class ReportingServiceInterpreter extends ReportingService[Amount] {

  def balanceByAccount: ReportOperation[Seq[(String, Amount)]] = Kleisli { (repo: AccountRepository) =>
    repo.all match {
      case Right(as) => as.map(a => (a.no, a.balance.amount)).asRight
      case Left(a) => a.asLeft
    }
  }
} 

object ReportingService extends ReportingServiceInterpreter
