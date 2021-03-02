package groupingn

import cats.effect.{ConcurrentEffect, Timer}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import models._

object Server {

  def stream[F[_]: ConcurrentEffect](implicit
      T: Timer[F],
      A: GroupingAlgebra[F]
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
