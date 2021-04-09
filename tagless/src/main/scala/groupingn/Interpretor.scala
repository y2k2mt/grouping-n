package groupingn.models.interpretors

import scala.util.Right
import monix.eval.Task
import groupingn.models._

object implicits {
  implicit val groupingAlgebra: GroupingAlgebra[Task] =
    MonixTaskGroupingInterpretor
}

object MonixTaskGroupingInterpretor extends GroupingAlgebra[Task] {

  override def grouping(
      candidates: Candidates
  ): Task[Either[GroupingError, Grouped]] =
    Task(
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

  import doobie.implicits._
  import groupingn.Database._
  import groupingn.Schedulers._
  import io.circe.generic.auto._, io.circe.syntax._, io.circe.parser.decode

  override def generateIdentity(
      grouped: Grouped
  ): Task[Either[GroupingError, IdentifiedGroup]] = {
    val uuid = java.util.UUID.randomUUID.toString
    val s    = grouped.asJson.noSpaces
    transactor
      .use { xa =>
        for {
          _ <-
            sql"insert into groupings (id,value) values ($uuid,$s)".update.run
              .transact(xa)
        } yield Right(IdentifiedGroup(uuid, grouped))
      }
      .executeOn(fixedPool)
  }

  override def identifiedGroup(
      uuid: String
  ): Task[Either[GroupingError, Option[IdentifiedGroup]]] =
    transactor
      .use { xa =>
        for {
          results <-
            sql"select id,value from groupings where id = $uuid"
              .query[(String, String)]
              .to[Array]
              .transact(xa)
          mayBeIdentified <- Task(
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
      .executeOn(fixedPool)
}
