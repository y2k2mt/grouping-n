package groupingn

import cats.effect.Async
import cats.implicits.*
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

object Routes {

  def groupingRoutes[F[_]: Async](G: GroupingAction[F]): HttpRoutes[F] = {

    implicit val candidatesDecoder: Decoder[Candidates] =
      deriveDecoder[Candidates]
    implicit def candidatesEntityDecoder: EntityDecoder[F, Candidates] =
      jsonOf[F, Candidates]

    implicit val groupEncoder: Encoder[Group] =
      deriveEncoder[Group]
    implicit val groupSeqEncoder: Encoder[Seq[Group]] =
      Encoder.encodeSeq[Group]
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

    HttpRoutes.of[F] {
      case req @ POST -> Root / "grouping" =>
        for {
          candidates <- req.as[Candidates]
          identified <- G.grouping(candidates)
          resp       <- identified.map(Ok(_)).left.map(handleError).merge
        } yield resp
      case GET -> Root / "grouping" / id =>
        for {
          identified <- G.identifiedGroup(id)
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
