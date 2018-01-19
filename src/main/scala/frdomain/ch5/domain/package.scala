package frdomain.ch5
package domain

import cats.data.NonEmptyList

package object service {
  type Valid[A] = Either[NonEmptyList[String], A]
}