package frdomain.ch7
package streams

import akka.actor.Actor
import akka.stream._
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString

import scala.collection.mutable.{Map => MMap}
import cats.syntax.monoid._

class Summarizer extends Actor with ActorSubscriber with Logging {
  private val balance = MMap.empty[String, Balance]

  private var inFlight = 0

  override protected def requestStrategy = new MaxInFlightRequestStrategy(10) {
    override def inFlightInternally = inFlight
  }

  def receive = {
    case OnNext(data: Transaction) =>
      inFlight += 1
      updateBalance(data)
      inFlight -= 1

    case LogSummaryBalance => logger.info("Balance so far: " + balance)
  }

  def updateBalance(data: Transaction) = balance.get(data.accountNo).fold { 
    balance += ((data.accountNo, Balance(data.amount, data.debitCredit)))
  } { b =>
    balance += ((data.accountNo, b |+| Balance(data.amount, data.debitCredit)))
  }
}



object Summarizer {
  def stage(name: String = "summarizer") = new GraphStage[FanInShape2[Transaction,LogCmd,Map[String,Balance]]] {
    val txIn = Inlet[Transaction](name + ".tx.in")
    val cmdIn = Inlet[LogCmd](name + ".cmd.in")
    val out = Outlet[Map[String,Balance]](name + ".out")

    override def shape = new FanInShape2(txIn, cmdIn, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
      private val balance = MMap.empty[String, Balance]

      def updateBalance(data: Transaction) = balance.get(data.accountNo).fold {
        balance += ((data.accountNo, Balance(data.amount, data.debitCredit)))
      } { b =>
        balance += ((data.accountNo, b |+| Balance(data.amount, data.debitCredit)))
      }

      setHandler(txIn, new InHandler {
        override def onPush(): Unit = {
          updateBalance(grab(txIn))
          tryPull(txIn)
        }
      })

      setHandler(cmdIn, new InHandler {
        override def onPush(): Unit = {
          grab(cmdIn)
          if (isAvailable(out))
            push(out, balance.toMap)
          tryPull(cmdIn)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          //nthng!!!???
        }
      })

      override def preStart(): Unit = {
        tryPull(txIn)
        tryPull(cmdIn)
      }
    }
  }
}