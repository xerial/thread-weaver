package com.github.j5ik2o.threadWeaver.adaptor.json

final case class AddMessagesRequestJson(senderId: String, messages: Seq[TextMessage], createAt: Long)