package com.github.j5ik2o.threadWeaver.adaptor.aggregates

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object DynamoDbConfig extends MultiNodeConfig {
  val controller = role("controller")
  val node1      = role("node1")
  val node2      = role("node2")

  testTransport(on = true)

  commonConfig(
    ConfigFactory
      .parseString(
        """
          |akka.loggers = ["akka.event.slf4j.Slf4jLogger"]
          |akka.loglevel = "DEBUG"
          |akka.logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
          |akka.actor.debug.receive = on
          |
          |akka.cluster.metrics.enabled=off
          |akka.actor.provider = "cluster"
          |
          |akka.persistence.journal.plugin = dynamo-db-journal
          |akka.persistence.snapshot-store.plugin = dynamo-db-snapshot
          |
          |passivate-timeout = 60 seconds
          |
          |dynamo-db-journal {
          |  dynamodb-client {
          |    access-key-id = "x"
          |    secret-access-key = "x"
          |    endpoint = "http://127.0.0.1:8000/"
          |  }
          |}
          |
          |dynamo-db-snapshot {
          |  dynamodb-client {
          |    access-key-id = "x"
          |    secret-access-key = "x"
          |    endpoint = "http://127.0.0.1:8000/"
          |  }
          |}
        """.stripMargin
      )
  )
}
