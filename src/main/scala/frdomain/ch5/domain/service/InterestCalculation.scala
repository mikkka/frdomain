package frdomain.ch5
package domain
package service

import cats.data.Kleisli

trait InterestCalculation[Account, Amount] {
  def computeInterest: Kleisli[Valid, Account, Amount]
}
