package groupingn.models.interpretors

import scala.util.Right
import cats.Applicative
import com.typesafe.scalalogging.LazyLogging
import cats.implicits.*
import cats.effect.*
import cats.mtl.*
import doobie.implicits.*
import io.circe.generic.auto.*, io.circe.syntax.*, io.circe.parser.decode
import groupingn.Database.*
import groupingn.models.*

object implicits {
  implicit val groupingAlgebra: GroupingAlgebra =
    GroupingInterpretor
}

object GroupingInterpretor extends GroupingAlgebra with LazyLogging {

  override def grouping[F[_]: Async](
      candidates: Candidates
  )(implicit F: Raise[F, GroupingError]): F[Grouped] =
    candidates.n match {
      case n if n < 2 => F.raise(InsufficientGroupingNumber(n))
      case n if candidates.members.size < n =>
        F.raise(InsufficientGroupingMember(n))
      case n =>
        Grouped(
          scala.util.Random
            .shuffle(candidates.members)
            .grouped(
              (candidates.members.size / n) + (candidates.members.size % n)
            )
            .map(Group(_))
            .toSeq
        ).pure[F]
    }

  override def generateIdentity[F[_]: Async](
      grouped: Grouped
  )(implicit F: Raise[F, GroupingError]): F[IdentifiedGroup] = {
    val uuid = java.util.UUID.randomUUID.toString
    val s    = grouped.asJson.noSpaces
    transactor[F]
      .use { xa =>
        logger.info(s"Add group : ${uuid}")
        for {
          _ <-
            sql"insert into groupings (id,value) values ($uuid,$s)".update.run
              .transact(xa)
        } yield IdentifiedGroup(uuid, grouped)
      }
  }

  override def identifiedGroup[F[_]: Async](
      uuid: String
  )(implicit F: Raise[F, GroupingError]): F[Option[IdentifiedGroup]] =
    transactor[F]
      .use { xa =>
        for {
          results <-
            sql"select id,value from groupings where id = $uuid"
              .query[(String, String)]
              .to[List]
              .transact(xa)
          mayBeIdentified <- results.headOption.pure[F]
          result <-
            mayBeIdentified
              .map { (_, v) =>
                decode[Grouped](v)
                  .map(IdentifiedGroup(uuid, _).some.pure[F])
                  .getOrElse(F.raise(InvalidGroupingDataFormatError(uuid)))
              }
              .getOrElse(F.raise(InvalidGroupingDataFormatError(uuid)))
        } yield result
      }
}
