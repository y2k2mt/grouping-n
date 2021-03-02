package groupingn

import zio._

object zioexample {

  // Data models

  case class Candidates(n: Int, members: Seq[String])
  case class Group(members: Seq[String])
  case class Grouped(groups: Seq[Group])
  case class IdentifiedGroup(group: Grouped)

  sealed trait GroupingError
  class InsufficientGroupingNumber extends GroupingError
  class InsufficientGroupingMember extends GroupingError

  // Repository component

  type GroupingRepository = Has[GroupingRepository.Service]

  object GroupingRepository {

    trait Service {
      def grouping(candidates: Candidates): IO[GroupingError, Grouped]
      def generateIdentity(
          grouped: Grouped
      ): IO[GroupingError, IdentifiedGroup]
    }

    def grouping(candidates: Candidates) =
      ZIO.accessM[GroupingRepository](_.get.grouping(candidates))
    def generateIdentity(grouped: Grouped) =
      ZIO.accessM[GroupingRepository](_.get.generateIdentity(grouped))

  }

  object GroupingRepositoryImpl extends GroupingRepository.Service {
    def grouping(candidates: Candidates): IO[GroupingError, Grouped] =
      ZIO.succeed(
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
    ): IO[GroupingError, IdentifiedGroup] =
      ZIO.succeed(IdentifiedGroup(grouped))

    val live: ULayer[GroupingRepository] = ZLayer.succeed(this)
  }

  // UseCase component

  object GroupingUseCase {
    def grouping(
        candidates: Candidates
    ): ZIO[GroupingRepository, GroupingError, IdentifiedGroup] =
      for {
        grouped    <- GroupingRepository.grouping(candidates)
        identified <- GroupingRepository.generateIdentity(grouped)
      } yield identified
  }

  // Main program

  object Application {

    def main(args: Array[String]): Unit = {
      val layer = GroupingRepositoryImpl.live
      val program =
        GroupingUseCase
          .grouping(Candidates(2, Seq("foo", "bar", "baz")))
          .provideLayer(layer)
      Runtime.default.unsafeRun(program)
    }
  }
}
