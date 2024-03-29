package com.github.j5ik2o.threadWeaver.adaptor.aggregates

import java.time.Instant

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.ActorRef
import com.github.j5ik2o.threadWeaver.domain.model.accounts.AccountId
import com.github.j5ik2o.threadWeaver.domain.model.threads._
import com.github.j5ik2o.threadWeaver.infrastructure.ulid.ULID
import com.github.j5ik2o.threadWeaver.adaptor.aggregates.ThreadProtocol._
import org.scalatest.{ FreeSpecLike, Matchers }

class ThreadAggregateSpec extends ScalaTestWithActorTestKit with FreeSpecLike with Matchers {

  def newThreadRef(threadId: ThreadId): ActorRef[CommandRequest] = spawn(ThreadAggregate.behavior(threadId, Seq.empty))

  "ThreadAggregate" - {
    "create" in {
      val threadId          = ThreadId()
      val threadRef         = newThreadRef(threadId)
      val now               = Instant.now
      val createThreadProbe = TestProbe[CreateThreadResponse]()
      val administratorId   = AccountId()
      val title             = ThreadTitle("test")
      threadRef ! CreateThread(
        ULID(),
        threadId,
        administratorId,
        None,
        title,
        None,
        AdministratorIds(administratorId),
        MemberIds.empty,
        now,
        Some(createThreadProbe.ref)
      )

      val createThreadSucceeded = createThreadProbe.expectMessageType[CreateThreadSucceeded]
      createThreadSucceeded.threadId shouldBe threadId
      createThreadSucceeded.createAt shouldBe now
    }
    "add administrator" in {
      val threadId          = ThreadId()
      val threadRef         = newThreadRef(threadId)
      val now               = Instant.now
      val createThreadProbe = TestProbe[CreateThreadResponse]()
      val administratorId   = AccountId()
      val title             = ThreadTitle("test")
      threadRef ! CreateThread(
        ULID(),
        threadId,
        administratorId,
        None,
        title,
        None,
        AdministratorIds(administratorId),
        MemberIds.empty,
        now,
        Some(createThreadProbe.ref)
      )

      createThreadProbe.expectMessageType[CreateThreadResponse] match {
        case f: CreateThreadFailed =>
          fail(f.message)
        case s: CreateThreadSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
      }

      val administratorId2                 = AccountId()
      val addAdministratorIdsResponseProbe = TestProbe[JoinAdministratorIdsResponse]()

      threadRef ! JoinAdministratorIds(
        ULID(),
        threadId,
        administratorId,
        AdministratorIds(administratorId2),
        now,
        Some(addAdministratorIdsResponseProbe.ref)
      )

      addAdministratorIdsResponseProbe.expectMessageType[JoinAdministratorIdsResponse] match {
        case f: JoinAdministratorIdsFailed =>
          fail(f.message)
        case s: JoinAdministratorIdsSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
      }

      val getAdministratorIdsResponseProbe = TestProbe[GetAdministratorIdsResponse]()

      threadRef ! GetAdministratorIds(ULID(), threadId, administratorId, now, getAdministratorIdsResponseProbe.ref)

      getAdministratorIdsResponseProbe.expectMessageType[GetAdministratorIdsResponse] match {
        case f: GetAdministratorIdsFailed =>
          fail(f.message)
        case s: GetAdministratorIdsSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
      }

    }
    "add members" in {
      val threadId          = ThreadId()
      val threadRef         = newThreadRef(threadId)
      val now               = Instant.now
      val createThreadProbe = TestProbe[CreateThreadResponse]()
      val administratorId   = AccountId()
      val title             = ThreadTitle("test")
      threadRef ! CreateThread(
        ULID(),
        threadId,
        administratorId,
        None,
        title,
        None,
        AdministratorIds(administratorId),
        MemberIds.empty,
        now,
        Some(createThreadProbe.ref)
      )

      createThreadProbe.expectMessageType[CreateThreadResponse] match {
        case f: CreateThreadFailed =>
          fail(f.message)
        case s: CreateThreadSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
      }

      val memberId                  = AccountId()
      val addMemberIdsResponseProbe = TestProbe[JoinMemberIdsResponse]()

      threadRef ! JoinMemberIds(
        ULID(),
        threadId,
        administratorId,
        MemberIds(memberId),
        now,
        Some(addMemberIdsResponseProbe.ref)
      )

      addMemberIdsResponseProbe.expectMessageType[JoinMemberIdsResponse] match {
        case f: JoinMemberIdsFailed =>
          fail(f.message)
        case s: JoinMemberIdsSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
      }

      val getMemberIdsResponseProbe = TestProbe[GetMemberIdsResponse]()

      threadRef ! GetMemberIds(ULID(), threadId, administratorId, now, getMemberIdsResponseProbe.ref)

      getMemberIdsResponseProbe.expectMessageType[GetMemberIdsResponse] match {
        case f: GetMemberIdsFailed =>
          fail(f.message)
        case s: GetMemberIdsSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
      }
    }
    "add messages" in {
      val threadId          = ThreadId()
      val threadRef         = newThreadRef(threadId)
      val now               = Instant.now
      val createThreadProbe = TestProbe[CreateThreadResponse]()
      val administratorId   = AccountId()
      val title             = ThreadTitle("test")
      threadRef ! CreateThread(
        ULID(),
        threadId,
        administratorId,
        None,
        title,
        None,
        AdministratorIds(administratorId),
        MemberIds.empty,
        now,
        Some(createThreadProbe.ref)
      )

      val createThreadSucceeded = createThreadProbe.expectMessageType[CreateThreadSucceeded]
      createThreadSucceeded.threadId shouldBe threadId
      createThreadSucceeded.createAt shouldBe now

      val memberId                  = AccountId()
      val addMemberIdsResponseProbe = TestProbe[JoinMemberIdsResponse]()

      threadRef ! JoinMemberIds(
        ULID(),
        threadId,
        administratorId,
        MemberIds(memberId),
        now,
        Some(addMemberIdsResponseProbe.ref)
      )

      addMemberIdsResponseProbe.expectMessageType[JoinMemberIdsResponse] match {
        case f: JoinMemberIdsFailed =>
          fail(f.message)
        case s: JoinMemberIdsSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
      }

      val addMessagesResponseProbe = TestProbe[AddMessagesResponse]()
      val messages                 = Messages(TextMessage(MessageId(), None, ToAccountIds.empty, Text("ABC"), memberId, now, now))
      threadRef ! AddMessages(
        ULID(),
        threadId,
        messages,
        now,
        Some(addMessagesResponseProbe.ref)
      )

      addMessagesResponseProbe.expectMessageType[AddMessagesResponse] match {
        case f: AddMessagesFailed =>
          fail(f.message)
        case s: AddMessagesSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
      }

      val getMessagesResponseProbe = TestProbe[GetMessagesResponse]()
      threadRef ! GetMessages(ULID(), threadId, memberId, now, getMessagesResponseProbe.ref)
      getMessagesResponseProbe.expectMessageType[GetMessagesResponse] match {
        case f: GetMessagesFailed =>
          fail(f.message)
        case s: GetMessagesSucceeded =>
          s.threadId shouldBe threadId
          s.createAt shouldBe now
          s.messages shouldBe messages
      }
    }
  }
}
