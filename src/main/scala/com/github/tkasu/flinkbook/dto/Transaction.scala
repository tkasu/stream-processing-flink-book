package com.github.tkasu.flinkbook.dto

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

  // CSV fields: trans_id,account_id,type,operation,amount,balance,k_symbol,fulldatewithtime,customer_id
  def fromCsvLine(s: String, eventTime: Long): Transaction =
    val fields = s.split(",")
    Transaction(
      fields(0),
      fields(1),
      fields(2),
      fields(3),
      fields(4).toDouble,
      fields(5).toDouble,
      fields(6),
      fields(7),
      fields(8),
      eventTime,
      // Not ISO-format, but need to be consistent with the book
      new Timestamp(eventTime).toString
    )

  implicit val decoder: JsonDecoder[Transaction] =
    DeriveJsonDecoder.gen[Transaction]

  implicit val encoder: JsonEncoder[Transaction] =
    DeriveJsonEncoder.gen[Transaction]
