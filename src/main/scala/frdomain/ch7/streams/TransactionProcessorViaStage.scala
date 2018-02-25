package frdomain.ch7.streams

import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, FlowShape, SinkShape}
import akka.stream.actor.ActorSubscriber
import akka.stream.scaladsl.{Framing, GraphDSL, RunnableGraph, Sink, Source, Tcp}
import akka.util.ByteString
import frdomain.ch7.streams.FrontOffice.logger

import scala.concurrent.duration._

/**
 * The TransactionProcessor that gets streaming input from FrontOffice, parses
 * the String and builds the Transaction object. The Summarizer actor reports the updated Balance
 * as it gets the transaction info.
 *
 * Run this as:
 *
 * $ sbt
 * > console
 * scala> import frdomain.ch7.streams._
 * scala> TransactionProcessor.main(Array(""))
 */
class TransactionProcessorViaStage(host: String, port: Int)(implicit val system: ActorSystem) extends Logging {

  def run(): Unit = {
    implicit val mat = ActorMaterializer()

    val summarizer = GraphDSL.create(Summarizer.stage()) { implicit b => sumGraph =>
      import GraphDSL.Implicits._

      Source.tick(1.second, 10.second, LogSummaryBalance) ~> sumGraph.in1

      FlowShape.of(sumGraph.in0, sumGraph.out)
    }

    logger.info(s"Receiver: binding to $host:$port")

    Tcp().bind(host, port).runForeach { conn =>
      val receiveSink =
        conn.flow
            .via(Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 4000, allowTruncation = true))
            .map(_.utf8String)
            .map(_.split(","))
            .mapConcat(Transaction(_).toList)
            .via(summarizer)
            .to(Sink.foreach{x => logger.info(s"$x")})

      receiveSink.runWith(Source.maybe)
    }
  }
}

object TransactionProcessorViaStage extends App {
  implicit val system = ActorSystem("processor")
  new TransactionProcessorViaStage("127.0.0.1", 9982).run()
}


