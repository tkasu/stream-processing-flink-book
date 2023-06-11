package com.github.tkasu.flinkbook.producers

import zio.*
import zio.json.*
import zio.stream.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.*

import java.nio.file.Paths
import com.github.tkasu.flinkbook.dto.Transaction

object TransactionsProducer extends ZIOAppDefault {

  private val fileName = "data/transactions.csv"
  private val parallelism = 1_000

  val readCsvStream: ZStream[Any, Nothing, Transaction] = ZStream
    .fromFileName(fileName)
    .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
    .drop(1)
    // TODO: Get time from ZIO instead
    .map(Transaction.fromCsvLine(_, java.time.Clock.systemUTC().millis()))
    .orDie

  def producerStream(transactionStream: ZStream[Any, Nothing, Transaction]) =
    transactionStream
      // .schedule(Schedule.spaced(2.seconds))
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
    _ <- producerStream(readCsvStream).runDrain
      .provide(producerLayer)
    _ <- Console.printLine("Transactions Producer finished")
  } yield ()

}
