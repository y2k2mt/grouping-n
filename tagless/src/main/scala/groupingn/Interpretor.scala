package groupingn.models.interpretors

import scala.util.Right
import com.typesafe.scalalogging.LazyLogging
import groupingn.models.*
import cats.effect.*
import cats.implicits.*

object implicits {
  implicit val groupingAlgebra: GroupingAlgebra =
    GroupingInterpretor
}

object GroupingInterpretor extends GroupingAlgebra with LazyLogging {

  override def grouping[F[_]: Async](
      candidates: Candidates
  )(implicit F: Async[F]): F[Either[GroupingError, Grouped]] =
    F.delay(
      candidates.n match {
        case n if n < 2 => Left(InsufficientGroupingNumber(n))
        case n if candidates.members.size < n =>
          Left(InsufficientGroupingMember(n))
        case n =>
          Right(
            Grouped(
              scala.util.Random
                .shuffle(candidates.members)
                .grouped(
                  (candidates.members.size / n) + (candidates.members.size % n)
                )
                .map(Group(_))
                .toSeq
            )
          )
      }
    )

  import doobie.implicits.*
  import groupingn.Database.*
  import io.circe.generic.auto.*, io.circe.syntax.*, io.circe.parser.decode

  override def generateIdentity[F[_]: Async](
      grouped: Grouped
  ): F[Either[GroupingError, IdentifiedGroup]] = {
    val uuid = java.util.UUID.randomUUID.toString
    val s    = grouped.asJson.noSpaces
    transactor[F]
      .use { xa =>
        logger.info(s"Add group : ${uuid}")
        for {
          _ <-
            sql"insert into groupings (id,value) values ($uuid,$s)".update.run
              .transact(xa)
        } yield Right(IdentifiedGroup(uuid, grouped))
      }
  }

  override def identifiedGroup[F[_]](
      uuid: String
  )(implicit F: Async[F]): F[Either[GroupingError, Option[IdentifiedGroup]]] =
    transactor[F]
      .use { xa =>
        for {
          results <-
            sql"select id,value from groupings where id = $uuid"
              .query[(String, String)]
              .to[Array]
              .transact(xa)
          mayBeIdentified <- F.delay(
            results.headOption match {
              case Some(r) =>
                decode[Grouped](r._2)
                  .map(Some(_))
                  .left
                  .map(_ => InvalidGroupingDataFormatError(uuid))
              case _ => Right(None)
            }
          )
        } yield mayBeIdentified.map(_.map(IdentifiedGroup(uuid, _)))
      }
}
