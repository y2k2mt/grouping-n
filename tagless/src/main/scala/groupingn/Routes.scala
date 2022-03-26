package groupingn

import cats.*
import cats.implicits.*
import cats.syntax.all.*
import cats.data.*
import cats.effect.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*

import io.circe.*
import io.circe.syntax.*
import io.circe.generic.semiauto.*

import org.http4s.*
import org.http4s.Status.*
import org.http4s.circe.*

import models.*
import models.interpretors.*

object Routes {

  def groupingRoutes[F[_]: Async]: HttpRoutes[F] = {

    implicit val candidatesDecoder: Decoder[Candidates] =
      deriveDecoder[Candidates]
    implicit def candidatesEntityDecoder: EntityDecoder[F, Candidates] =
      jsonOf[F, Candidates]

    implicit val groupEncoder: Encoder[models.Group] =
      deriveEncoder[models.Group]
    implicit val groupSeqEncoder: Encoder[Seq[models.Group]] =
      Encoder.encodeSeq[models.Group]
    implicit val identifiedGroupEncoder: Encoder[IdentifiedGroup] =
      deriveEncoder[IdentifiedGroup]
    implicit val identifiedGroupSeqEncoder: Encoder[Seq[IdentifiedGroup]] =
      Encoder.encodeSeq[IdentifiedGroup]
    implicit def identifiedGroupEntityEncoder
        : EntityEncoder[F, IdentifiedGroup] = jsonEncoderOf[F, IdentifiedGroup]
    implicit val groupingErrorEncoder: Encoder[GroupingError] =
      deriveEncoder[GroupingError]
    implicit def groupingErrorEntityEncoder: EntityEncoder[F, GroupingError] =
      jsonEncoderOf[F, GroupingError]

    val dsl = new Http4sDsl[F] {}

    import dsl.*
    type K = Kleisli[F, GroupingAlgebra, _]
    type E = EitherT[K, GroupingError, _]

    HttpRoutes.of[F] {
      case req @ POST -> Root / "grouping" =>
        for {
          candidates <- req.as[Candidates]
          identified <-
            GroupingAction
              .grouping[E](candidates)
              .value
              .run(GroupingInterpretor)
          resp <- identified.map(Ok(_)).left.map(handleError).merge
        } yield resp
      case GET -> Root / "grouping" / id =>
        for {
          identified <-
            GroupingAction.identifiedGroup[E](id).value.run(GroupingInterpretor)
          resp <-
            identified
              .map(_.map(Ok(_)).getOrElse(NotFound()))
              .left
              .map(handleError)
              .merge
        } yield resp
    }

  }

  def handleError[F[_]: Async](implicit
      F: Async[F]
  ): GroupingError => F[Response[F]] = {
    case InsufficientGroupingMember(require) =>
      F.pure(
        Response[F](status = BadRequest).withEntity(
          ("message" -> s"The number of groups must be greater than $require of members.").asJson
        )
      )
    case InsufficientGroupingNumber(number) =>
      F.pure(
        Response[F](status = BadRequest).withEntity(
          ("message" -> s"The number of groups must be at least $number.").asJson
        )
      )
    case InvalidGroupingDataFormatError(id) =>
      F.pure(
        Response[F](status = InternalServerError)
          .withEntity(
            ("message" -> s"The data format is collapsed :$id").asJson
          )
      )
  }

}
