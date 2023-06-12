package com.github.tkasu.flinkbook.dto

import fs2.*
import data.csv.*
import zio.json.*

import java.sql.Timestamp

case class Transaction(
    transactionId: String,
    accountId: String,
    @jsonField("type") transType: String,
    operation: String,
    amount: Double,
    balance: Double,
    kSymbol: String,
    fullDateWithTime: String,
    customerId: String,
    eventTime: Long,
    eventTimeFormatted: String
)

object Transaction:

  implicit val jsonDecoder: JsonDecoder[Transaction] =
    DeriveJsonDecoder.gen[Transaction]

  implicit val jsonEncoder: JsonEncoder[Transaction] =
    DeriveJsonEncoder.gen[Transaction]

  implicit val csvDecoder: CsvRowDecoder[Transaction, String] =
    row =>
      for {
        transactionId <- row.as[String]("trans_id")
        accountId <- row.as[String]("account_id")
        transType <- row.as[String]("type")
        operation <- row.as[String]("operation")
        amount <- row.as[Double]("amount")
        balance <- row.as[Double]("balance")
        kSymbol <- row.as[String]("k_symbol")
        fullDateWithTime <- row.as[String]("fulldatewithtime")
        customerId <- row.as[String]("customer_id")
        // Use current time for testing purposes, similar approach than in the book
        eventTime = System.currentTimeMillis()
        // Not ISO-format, but need to be consistent with the book
        eventTimeFormatted = new Timestamp(eventTime).toString
      } yield Transaction(
        transactionId,
        accountId,
        transType,
        operation,
        amount,
        balance,
        kSymbol,
        fullDateWithTime,
        customerId,
        eventTime,
        eventTimeFormatted
      )
