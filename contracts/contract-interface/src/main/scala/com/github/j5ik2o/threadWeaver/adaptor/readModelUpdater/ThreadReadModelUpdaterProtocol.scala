package com.github.j5ik2o.threadWeaver.adaptor.readModelUpdater

import java.time.Instant

import com.github.j5ik2o.threadWeaver.domain.model.threads.ThreadId
import com.github.j5ik2o.threadWeaver.infrastructure.ulid.ULID

object ThreadReadModelUpdaterProtocol {
  sealed trait Message

  sealed trait CommandMessage extends Message {
    def id: ULID
    def threadId: ThreadId
    def createAt: Instant
  }
  sealed trait CommandRequest extends CommandMessage

  sealed trait CommandResponse extends CommandMessage {
    def requestId: ULID
  }

  final case class Start(id: ULID, threadId: ThreadId, createAt: Instant) extends CommandRequest
  final case class Stop(id: ULID, threadId: ThreadId, createAt: Instant)  extends CommandRequest

  case object Idle extends CommandRequest {
    override def id: ULID           = throw new UnsupportedOperationException
    override def threadId: ThreadId = throw new UnsupportedOperationException
    override def createAt: Instant  = throw new UnsupportedOperationException
  }

  case object Stop extends CommandRequest {
    override def id: ULID           = throw new UnsupportedOperationException
    override def threadId: ThreadId = throw new UnsupportedOperationException
    override def createAt: Instant  = throw new UnsupportedOperationException
  }

}
