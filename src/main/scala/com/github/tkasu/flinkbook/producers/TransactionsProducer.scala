package com.github.tkasu.flinkbook.producers

import fs2.Stream
import fs2.io.file.{Files, Path}
import fs2.data.csv.lenient.attemptDecodeUsingHeaders
import zio.*
import zio.json.*
import zio.stream.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.*
import zio.interop.catz.*
import zio.stream.interop.fs2z.*

import java.nio.file.Paths
import com.github.tkasu.flinkbook.dto.Transaction

import scala.io.Source

object TransactionsProducer extends ZIOAppDefault {

  private val fileName = "data/transactions.csv"
  private val parallelism = 1_000

  def producerStream(transactionStream: ZStream[Any, Nothing, Transaction]) =
    transactionStream
      .mapZIOParUnordered(parallelism) { transaction =>
        Producer.produce[Any, String, String](
          topic = "transactions",
          key = transaction.transactionId,
          value = transaction.toJson,
          keySerializer = Serde.string,
          valueSerializer = Serde.string
        )
      }

  override def run = for {
    _ <- Console.printLine("Starting Transactions Producer")
    producerLayer = ZLayer.scoped(
      Producer.make(
        settings = ProducerSettings(List("localhost:19092"))
      )
    )

    readFileStream = Files[Task]
      .readUtf8(Path(fileName))

    parseCsvTask =
      readFileStream
        .through(attemptDecodeUsingHeaders[Transaction]())
        .toZStream(parallelism)
        // Log and discard errors
        .tap(mbyTransaction =>
          if (mbyTransaction.isLeft) Console.printLineError(mbyTransaction)
          else ZIO.unit
        )
        .collect { case Right(transaction) => transaction }

    /* For parse debugging
    _ <- parseCsvTask
      .tap(Console.printLine(_))
      .runDrain
     */

    _ <- producerStream(parseCsvTask.orDie).zipWithIndex
      .tap { case (_, idx) =>
        if ((idx + 1) % 10_000 == 0)
          Console.printLine(s"Processed $idx transactions.")
        else ZIO.unit
      }
      .runDrain
      .provide(producerLayer)

    _ <- Console.printLine("Transactions Producer finished")
  } yield ()

}
