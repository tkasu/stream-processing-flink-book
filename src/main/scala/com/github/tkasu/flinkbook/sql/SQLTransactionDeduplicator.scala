package com.github.tkasu.flinkbook.sql

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment

object SQLTransactionDeduplicator {

  val configuration = new Configuration()

  val env = StreamExecutionEnvironment
    .createLocalEnvironment(configuration)
    .setParallelism(5)

  val tableEnv = StreamTableEnvironment.create(env)

  def createBankDatabase(): Unit = {
    tableEnv.executeSql("CREATE DATABASE bank").print()
  }

  def createTransactionsTable(port: Int): Unit = {
    val query =
      s"""
        |CREATE TABLE bank.transactions (
        |   transactionId STRING,
        |   accountId STRING,
        |   customerId STRING,
        |   eventTime BIGINT,
        |   eventTime_ltz AS TO_TIMESTAMP_LTZ(eventTime, 3),
        |   eventTimeFormatted STRING,
        |   type STRING,
        |   operation STRING,
        |   amount DOUBLE,
        |   balance DOUBLE,
        |   // `ts` TIMESTAMP(3) METADATA FROM 'timestamp'
        | WATERMARK FOR eventTime_ltz AS eventTime_ltz
        | ) WITH (
        |   'connector' = 'kafka',
        |   'topic' = 'transactions',
        |   'properties.bootstrap.servers' = 'localhost:$port',
        |   'properties.group.id' = 'group.transactions',
        |   'format' = 'json',
        |   'scan.startup.mode' = 'earliest-offset'
        | );
        |""".stripMargin
    tableEnv.executeSql(query).print()
  }

  def runTransactionDeduplicationQuery(): Unit = {
    val query =
      """
        |WITH transaction_with_rownum AS (
        |    SELECT
        |        *,
        |        ROW_NUMBER() OVER (
        |            PARTITION BY transactionId
        |            ORDER BY eventTime_ltz
        |        ) AS rowNum
        |    FROM bank.transactions
        |)
        |SELECT
        |    *
        |FROM
        |    transaction_with_rownum
        |WHERE
        |    rowNum = 1;
        |""".stripMargin
    tableEnv.executeSql(query).print()
  }

  def main(args: Array[String]): Unit = {
    val portOutsideDocker = 19092
    // val portWithinDocker = 9092
    createBankDatabase()
    createTransactionsTable(port = portOutsideDocker)
    runTransactionDeduplicationQuery()
  }

}
