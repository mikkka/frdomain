package frdomain.ch7
package streams

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage._
import akka.util.ByteString

import scala.annotation.tailrec


object Stages {
  def parseLines(separator: String, maximumLineBytes: Int, name: String = "parseLines") =
    new GraphStage[FlowShape[ByteString, String]] {
      val in = Inlet[ByteString](name + ".in")
      val out = Outlet[String](name + ".out")

      override def shape = FlowShape.of(in, out)

      override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {
        private val separatorBytes = ByteString(separator)
        private val firstSeparatorByte = separatorBytes.head
        private var buffer = ByteString.empty
        private var nextPossibleMatch = 0

        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val chunk = grab(in)
            buffer ++= chunk
            if (buffer.size > maximumLineBytes)
              fail(out, new IllegalStateException(s"Read ${buffer.size} bytes " +
                s"which is more than $maximumLineBytes without seeing a line terminator"))
            else emitMultiple(out, doParse(Vector.empty).iterator)
          }
        })

        setHandler(out, new OutHandler {
          override def onPull() = pull(in)
        })

        @tailrec
        private def doParse(parsedLinesSoFar: Vector[String]): Vector[String] = {
          val possibleMatchPos = buffer.indexOf(firstSeparatorByte, from = nextPossibleMatch)
          if (possibleMatchPos == -1) {
            // No matching character, we need to accumulate more bytes into the buffer
            nextPossibleMatch = buffer.size
            parsedLinesSoFar
          } else {
            if (possibleMatchPos + separatorBytes.size > buffer.size) {
              // We have found a possible match (we found the first character of the terminator
              // sequence) but we don't have yet enough bytes. We remember the position to
              // retry from next time.
              nextPossibleMatch = possibleMatchPos
              parsedLinesSoFar
            } else {
              if (buffer.slice(possibleMatchPos, possibleMatchPos + separatorBytes.size)
                == separatorBytes) {
                // Found a match
                val parsedLine = buffer.slice(0, possibleMatchPos).utf8String
                buffer = buffer.drop(possibleMatchPos + separatorBytes.size)
                nextPossibleMatch -= possibleMatchPos + separatorBytes.size
                doParse(parsedLinesSoFar :+ parsedLine)
              } else {
                nextPossibleMatch += 1
                doParse(parsedLinesSoFar)
              }
            }
          }

        }
      }
    }
}
