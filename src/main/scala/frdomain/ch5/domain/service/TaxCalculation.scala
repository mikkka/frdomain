package frdomain.ch5
package domain
package service

import cats.data.Kleisli

trait TaxCalculation[Amount] {
  def computeTax: Kleisli[Valid, Amount, Amount]
}
