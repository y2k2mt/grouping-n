package groupingn

import cats.effect.Async
import fs2.Stream
import org.http4s.implicits.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import models.*

object Server {

  def stream[F[_]: Async](implicit
      A: GroupingAlgebra
  ): Stream[F, Nothing] = {
    val groupingAction = GroupingAction.impl[F]
    // Whole routes
    val httpApp = (
      Routes.groupingRoutes[F](groupingAction)
    ).orNotFound

    // With Middlewares in place
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)
    for {
      exitCode <-
        BlazeServerBuilder[F]
          .bindHttp(8080, "0.0.0.0")
          .withHttpApp(finalHttpApp)
          .serve
    } yield exitCode
  }.drain

}
