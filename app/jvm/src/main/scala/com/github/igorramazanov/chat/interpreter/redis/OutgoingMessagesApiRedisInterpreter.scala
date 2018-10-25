package com.github.igorramazanov.chat.interpreter.redis

import cats.effect.{Async, Timer}
import cats.syntax.all._
import com.github.igorramazanov.chat.Utils._
import com.github.igorramazanov.chat.api.OutgoingMessagesApi
import com.github.igorramazanov.chat.domain.ChatMessage.GeneralChatMessage
import com.github.igorramazanov.chat.json.DomainEntitiesJsonSupport
import org.slf4j.LoggerFactory
import scredis.Redis

import scala.concurrent.ExecutionContext

class OutgoingMessagesApiRedisInterpreter[F[_]: Async: Timer] private (
    redis: Redis)(implicit ec: ExecutionContext,
                  jsonSupport: DomainEntitiesJsonSupport)
    extends OutgoingMessagesApi[F] {
  private val logger = LoggerFactory.getLogger(this.getClass)

  override def send(generalChatMessage: GeneralChatMessage): F[Unit] = {
    import DomainEntitiesJsonSupport._
    import jsonSupport._
    logger.debug(
      s"Sending message from '${generalChatMessage.from}' to '${generalChatMessage.to}'")

    liftFromFuture(
      redis.publish(generalChatMessage.to, generalChatMessage.toJson),
      logger.error(
        s"Couldn't publish message from user '${generalChatMessage.from}' to '${generalChatMessage.to}'",
        _)
    ).map(_.discard())

  }
}

object OutgoingMessagesApiRedisInterpreter {
  def apply[F[_]: Async: Timer](redis: Redis)(
      implicit ec: ExecutionContext,
      jsonSupport: DomainEntitiesJsonSupport)
    : OutgoingMessagesApiRedisInterpreter[F] =
    new OutgoingMessagesApiRedisInterpreter[F](redis)
}