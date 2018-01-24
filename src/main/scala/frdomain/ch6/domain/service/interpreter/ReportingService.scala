package frdomain.ch6
package domain
package service
package interpreter


import cats.data.{EitherT, Kleisli}
import cats.syntax.either._

import scala.concurrent._
import ExecutionContext.Implicits.global
import repository.AccountRepository
import model.common._


class ReportingServiceInterpreter extends ReportingService[Amount] {

  def balanceByAccount: ReportOperation[Seq[(String, Amount)]] = Kleisli[Valid, AccountRepository, Seq[(String, Amount)]] { (repo: AccountRepository) =>
    EitherT {
      Future {
        repo.all match {
          case Right(as) => as.map(a => (a.no, a.balance.amount)).asRight
          case Left(a) => a.asLeft
        }
      }
    }
  }
} 

object ReportingService extends ReportingServiceInterpreter
