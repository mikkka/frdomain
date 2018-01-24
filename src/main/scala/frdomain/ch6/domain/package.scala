package frdomain.ch6
package domain

import scala.concurrent.Future
import cats.data.EitherT
import cats.data.NonEmptyList

package object service {
  type Valid[A] = EitherT[Future, NonEmptyList[String], A]
}
