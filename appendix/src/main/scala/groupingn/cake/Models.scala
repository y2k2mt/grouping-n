package groupingn

import scala.util.Right
import scala.concurrent.Future
import cats.data.EitherT

object cake {

  implicit val ec = scala.concurrent.ExecutionContext.global

  // Data models

  case class Candidates(n: Int, members: Seq[String])
  case class Group(members: Seq[String])
  case class Grouped(groups: Seq[Group])
  case class IdentifiedGroup(group: Grouped)

  trait GroupingError
  class InsufficientGroupingNumber extends GroupingError
  class InsufficientGroupingMember extends GroupingError

  // Repository component

  trait GroupingRepository {
    def grouping(candidates: Candidates): Future[Either[GroupingError, Grouped]]
    def generateIdentity(
        grouped: Grouped
    ): Future[Either[GroupingError, IdentifiedGroup]]
  }

  object GroupingRepository extends GroupingRepository {
    def grouping(
        candidates: Candidates
    ): Future[Either[GroupingError, Grouped]] = Future.successful(
      candidates.n match {
        case n if n < 2 => Left(InsufficientGroupingNumber(n))
        case n if candidates.members.size < n => Left(InsufficientGroupingMember(n))
        case n => Right(Grouped(
          scala.util.Random.shuffle(candidates.members)
            .grouped(
              (candidates.members.size / n) + (candidates.members.size % n)
            )
            .map(Group(_))
            .toSeq
          ))
      }
    )

    def generateIdentity(
        grouped: Grouped
    ): Future[Either[GroupingError, IdentifiedGroup]] =
      Future(Right(IdentifiedGroup(grouped)))
  }

  // Mix-in component

  trait MixinGroupingRepository {
    def groupingRepository: GroupingRepository
  }

  trait MixinGroupingRepositoryImpl extends MixinGroupingRepository {
    override val groupingRepository: GroupingRepository = GroupingRepository
  }

  // UseCase component

  trait GroupingUseCase { self: MixinGroupingRepository =>

    def grouping(
        candidates: Candidates
    ): EitherT[Future, GroupingError, IdentifiedGroup] =
      for {
        grouped    <- EitherT(groupingRepository.grouping(candidates))
        identified <- EitherT(groupingRepository.generateIdentity(grouped))
      } yield identified

  }

  object GroupingUseCase
      extends GroupingUseCase
      with MixinGroupingRepositoryImpl

  // Main program

  object Application {
    import scala.concurrent.Await
    import scala.concurrent.duration._
    def main(args: Array[String]) =
      println(
        Await
          .result(
            GroupingUseCase
              .grouping(Candidates(2, Seq("foo", "bar", "baz")))
              .value,
            1.second
          )
      )
  }
}
