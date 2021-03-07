package groupingn

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._

object Main {

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      //TODO: persist into actor
      //implicit val actor = context.spawn(GroupingNActor(), "GroupingNActor")
      //context.watch(actor)
      implicit val ac = context.system
      for {
        conf <- ConfigSource.fromConfig(ConfigFactory.load()).load[AppConfig]
        _    <- Right(Server.startHttp(Routes.groupingRoutes, conf))
      } yield ()

      Behaviors.empty
    }
    ActorSystem[Nothing](rootBehavior, "AkkaHttpServer")
  }
}

case class AppConfig(server: ServerConfig)
case class ServerConfig(host: String, port: Int)
