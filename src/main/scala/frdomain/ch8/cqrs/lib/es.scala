package frdomain.ch8
package cqrs.lib

import cats.free.Free
import org.joda.time.DateTime
import cats.syntax.either._
import cats.~>
import monix.eval.Task

object Common {
  type AggregateId = String
  type Error = String
}

import Common._

/**
 * The `Event` abstraction. `Next` points to the next event in chain
 */
trait Event[A] {
  def at: DateTime
}

/**
 * All aggregates need to have an id
 */
trait Aggregate {
  def id: AggregateId
}

trait Snapshot[A <: Aggregate] {
  def updateState(e: Event[_], initial: Map[String, A]): Map[String, A]

  def snapshot(es: List[Event[_]]): Either[String, Map[String, A]] =
    es.reverse.foldLeft(Map.empty[String, A]) { (a, e) => updateState(e, a) }.asRight
}

trait Commands[A] {
  type Command[A] = Free[Event, A]
}

trait RepositoryBackedInterpreter {
  def step: Event ~> Task

  def apply[A](action: Free[Event, A]): Task[A] = action.foldMap(step)
}
