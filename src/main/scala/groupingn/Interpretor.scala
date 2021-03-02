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
  import io.circe.generic.auto._, io.circe.syntax._

  override def generateIdentity(
      grouped: Grouped
  ): Task[Either[GroupingError, IdentifiedGroup]] = {
    val uuid = java.util.UUID.randomUUID.toString
    val s    = grouped.asJson.noSpaces
    transactor.use { xa =>
      for {
        _ <-
          sql"insert into groupings (id,value) values ($uuid,$s)".update.run
            .transact(xa)
      } yield Right(IdentifiedGroup(uuid, grouped))
    }
  }
}