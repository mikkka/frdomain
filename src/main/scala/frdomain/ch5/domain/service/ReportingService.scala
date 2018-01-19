package frdomain.ch5
package domain
package service

import cats.data.Kleisli

import repository.AccountRepository

trait ReportingService[Amount] {
  type ReportOperation[A] = Kleisli[Valid, AccountRepository, A]

  def balanceByAccount: ReportOperation[Seq[(String, Amount)]]
} 
