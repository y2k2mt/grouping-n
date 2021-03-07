package groupingn

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.Failure
import scala.util.Success

object Server {

  def startHttp(routes: Route, config: AppConfig)(implicit
      system: ActorSystem[_]
  ): Unit = {
    import system.executionContext

    val futureBinding =
      Http().newServerAt(config.server.host, config.server.port).bind(routes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "Server online at http://{}:{}/",
          address.getHostString,
          address.getPort
        )
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

}
