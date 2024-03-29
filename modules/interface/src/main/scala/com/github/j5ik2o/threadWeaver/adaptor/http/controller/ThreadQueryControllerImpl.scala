package com.github.j5ik2o.threadWeaver.adaptor.http.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Sink
import com.github.j5ik2o.threadWeaver.adaptor.das.ThreadDas
import com.github.j5ik2o.threadWeaver.adaptor.http.directives.{ MetricsDirectives, ThreadValidateDirectives }
import com.github.j5ik2o.threadWeaver.adaptor.http.json._
import com.github.j5ik2o.threadWeaver.adaptor.http.rejections.{ NotFoundRejection, RejectionHandlers }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import kamon.context.Context
import wvlet.airframe._

trait ThreadQueryControllerImpl extends ThreadQueryController with ThreadValidateDirectives with MetricsDirectives {
  private val threadDas = bind[ThreadDas]

  override def toRoutes(implicit context: Context): Route = handleRejections(RejectionHandlers.default) {
    pathPrefix("v1") {
      getThread ~ getThreads ~ getAdministratorIds ~ getMemberIds ~ getMessages
    }
  }

  override private[controller] def getThread(implicit context: Context) = traceName(context)("get-thread") {
    path("threads" / Segment) { threadIdString =>
      get {
        extractExecutionContext { implicit ec =>
          extractMaterializer { implicit mat =>
            validateThreadId(threadIdString) { threadId =>
              onSuccess(
                threadDas
                  .getThreadByIdSource(threadId)
                  .map { threadRecord =>
                    ThreadJson(
                      threadRecord.id,
                      threadRecord.creatorId,
                      threadRecord.parentId,
                      threadRecord.title,
                      threadRecord.remarks,
                      threadRecord.createdAt.toEpochMilli,
                      threadRecord.updatedAt.toEpochMilli
                    )
                  }.runWith(Sink.headOption[ThreadJson]).map(identity)
              ) {
                case None =>
                  reject(NotFoundRejection("thread is not found", None))
                case Some(response) =>
                  complete(GetThreadResponseJson(response))
              }

            }
          }
        }
      }
    }
  }

  override private[controller] def getThreads(implicit context: Context): Route = traceName(context)("get-threads") {
    path("threads") {
      get {
        extractExecutionContext { implicit ec =>
          extractMaterializer { implicit mat =>
            parameters(('account_id.as[String], 'offset.as[Long].?, 'limit.as[Long].?)) {
              case (accountIdString, offset, limit) =>
                validateAccountId(accountIdString) { accountId =>
                  onSuccess(
                    threadDas
                      .getThreadsByAccountIdSource(accountId, offset, limit)
                      .map { threadRecord =>
                        ThreadJson(
                          threadRecord.id,
                          threadRecord.creatorId,
                          threadRecord.parentId,
                          threadRecord.title,
                          threadRecord.remarks,
                          threadRecord.createdAt.toEpochMilli,
                          threadRecord.updatedAt.toEpochMilli
                        )
                      }.runWith(Sink.seq[ThreadJson]).map(_.toSeq)
                  ) { response =>
                    complete(GetThreadsResponseJson(response))
                  }
                }
            }
          }
        }
      }
    }
  }

  override private[controller] def getAdministratorIds(implicit context: Context): Route =
    traceName(context)("get-administrator-ids") {
      path("threads" / Segment / "administrator-ids") { threadIdString =>
        get {
          extractExecutionContext { implicit ec =>
            extractMaterializer { implicit mat =>
              validateThreadId(threadIdString) { threadId =>
                parameters(('offset.as[Long].?, 'limit.as[Long].?)) {
                  case (offset, limit) =>
                    onSuccess(
                      threadDas
                        .getAdministratorsByThreadIdSource(threadId, offset, limit)
                        .map { record =>
                          record.accountId
                        }.runWith(Sink.seq[String]).map(_.toSeq)
                    ) { response =>
                      complete(GetThreadAdministratorIdsResponseJson(response))
                    }
                }
              }
            }
          }
        }
      }
    }

  override private[controller] def getMemberIds(implicit context: Context) =
    traceName(context)("get-member-ids") {
      path("threads" / Segment / "member-ids") { threadIdString =>
        get {
          extractExecutionContext { implicit ec =>
            extractMaterializer { implicit mat =>
              validateThreadId(threadIdString) { threadId =>
                parameters(('offset.as[Long].?, 'limit.as[Long].?)) {
                  case (offset, limit) =>
                    onSuccess(
                      threadDas
                        .getMembersByThreadIdSource(threadId, offset, limit)
                        .map { record =>
                          record.accountId
                        }.runWith(Sink.seq[String]).map(_.toSeq)
                    ) { response =>
                      complete(GetThreadMemberIdsResponseJson(response))
                    }
                }
              }
            }
          }
        }
      }
    }

  override private[controller] def getMessages(implicit context: Context): Route = traceName(context)("get-messages") {
    path("threads" / Segment / "messages") { threadIdString =>
      get {
        extractExecutionContext { implicit ec =>
          extractMaterializer { implicit mat =>
            validateThreadId(threadIdString) { threadId =>
              parameters(('offset.as[Long].?, 'limit.as[Long].?)) {
                case (offset, limit) =>
                  onSuccess(
                    threadDas
                      .getMessagesByThreadIdSource(threadId, offset, limit)
                      .map { messageRecord =>
                        ThreadMessageJson(
                          messageRecord.id,
                          messageRecord.threadId,
                          messageRecord.senderId,
                          messageRecord.`type`,
                          messageRecord.body,
                          messageRecord.createdAt.toEpochMilli,
                          messageRecord.updatedAt.toEpochMilli
                        )
                      }.runWith(Sink.seq[ThreadMessageJson]).map(_.toSeq)
                  ) { response =>
                    complete(GetThreadMessagesResponseJson(response))
                  }
              }
            }
          }
        }
      }
    }
  }
}
