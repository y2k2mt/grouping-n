package groupingn

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto._

import org.http4s._
import org.http4s.circe._

import models._

object Routes {

  def groupingRoutes[F[_]: Sync](G: GroupingAction[F]): HttpRoutes[F] = {

    implicit val decoder = jsonOf[F, Candidates]

    implicit val identifiedGroupEncoder: Encoder[IdentifiedGroup] =
      deriveEncoder[IdentifiedGroup]
    implicit def identifiedGroupEntityEncoder[A[_]]
        : EntityEncoder[A, IdentifiedGroup] = jsonEncoderOf[A, IdentifiedGroup]
    implicit val groupingErrorEncoder: Encoder[GroupingError] =
      deriveEncoder[GroupingError]
    implicit def groupingErrorEntityEncoder[A[_]]
        : EntityEncoder[A, GroupingError] = jsonEncoderOf[A, GroupingError]

    val dsl = new Http4sDsl[F] {}

    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "grouping" =>
        for {
          candidates <- req.as[Candidates]
          identified <- G.grouping(candidates)
          resp       <- identified.map(Ok(_)).left.map(BadRequest(_)).merge
        } yield resp
      case GET -> Root / "grouping" / id =>
        for {
          identified <- G.identifiedGroup(id)
          resp <-
            identified
              .map(_.map(Ok(_)).getOrElse(NotFound()))
              .left
              .map(BadRequest(_))
              .merge
        } yield resp
    }
  }
}
