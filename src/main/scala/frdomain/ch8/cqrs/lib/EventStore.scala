package frdomain.ch8
package cqrs.lib

import collection.concurrent.TrieMap
import cats.syntax.either._

import Common._
import spray.json._

trait EventStore[K] {
  /**
   * gets the list of events for an aggregate key `key`
   */
  def get(key: K): List[Event[_]]

  /**
   * puts a `key` and its associated `event`
   */
  def put(key: K, event: Event[_]): Either[Error, Event[_]]

  /**
   * similar to `get` but returns an error if the `key` is not found
   */
  def events(key: K): Either[Error, List[Event[_]]]
  
  /**
   * get all ids from the event store
   */
  def allEvents: Either[Error, List[Event[_]]]
}

/**
 * In memory store
 */
object InMemoryEventStore {
  def apply[K] = new EventStore[K] {
    val eventLog = TrieMap[K, List[Event[_]]]() 

    def get(key: K): List[Event[_]] = eventLog.get(key).getOrElse(List.empty[Event[_]])
    def put(key: K, event: Event[_]): Either[Error, Event[_]] = {
      val currentList = eventLog.getOrElse(key, Nil)
      eventLog += (key -> (event :: currentList))
      event.asRight
    }
    def events(key: K): Either[Error, List[Event[_]]] = {
      val currentList = eventLog.getOrElse(key, Nil)
      if (currentList.isEmpty) s"Aggregate $key does not exist".asLeft
      else currentList.asRight
    }
    def allEvents: Either[Error, List[Event[_]]] = eventLog.values.toList.flatten.asRight
  }
}
      
/**
 * In memory json store
 */
trait InMemoryJSONEventStore { 
  implicit val eventJsonFormat: RootJsonFormat[Event[_]]
  def apply[K] = new EventStore[K] {
    val eventLog = TrieMap[K, List[String]]() 

    def get(key: K): List[Event[_]] = 
      eventLog.get(key).map(ls => ls.map(_.parseJson.convertTo[Event[_]])).getOrElse(List.empty[Event[_]])

    def put(key: K, event: Event[_]): Either[Error, Event[_]] = {
      val currentList = eventLog.getOrElse(key, Nil)
      eventLog += (key -> (eventJsonFormat.write(event).toString :: currentList))
      event.asRight
    }
    def events(key: K): Either[Error, List[Event[_]]] = {
      val currentList = eventLog.getOrElse(key, Nil)
      if (currentList.isEmpty) s"Aggregate $key does not exist".asLeft
      else currentList.map(js => js.parseJson.convertTo[Event[_]]).asRight
    }
    def allEvents: Either[Error, List[Event[_]]] = eventLog.values.toList.flatten.map(_.parseJson.convertTo[Event[_]]).asRight
  }
}
      

