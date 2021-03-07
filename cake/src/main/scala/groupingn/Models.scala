package groupingn.models

import scala.util.Right
import scala.concurrent.Future
import cats.data.EitherT

// Data models

final case class Candidates(n: Int, members: Seq[String])
final case class Group(members: Seq[String])
final case class Grouped(groups: Seq[Group])
final case class IdentifiedGroup(id: String, group: Grouped)

sealed trait GroupingError
final case class InsufficientGroupingNumber(val number: Int)
    extends GroupingError
final case class InsufficientGroupingMember(val require: Int)
    extends GroupingError
final case class InvalidGroupingDataFormatError(val id: String)
    extends GroupingError

// Repository component

trait GroupingRepository {
  def grouping(candidates: Candidates): Future[Either[GroupingError, Grouped]]
  def generateIdentity(
      grouped: Grouped
  ): Future[Either[GroupingError, IdentifiedGroup]]
  def identifiedGroup(
      uuid: String
  ): Future[Either[GroupingError, Option[IdentifiedGroup]]]
}

object GroupingRepository extends GroupingRepository {
  def grouping(
      candidates: Candidates
  ): Future[Either[GroupingError, Grouped]] =
    Future.successful(
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

  def generateIdentity(
      grouped: Grouped
  ): Future[Either[GroupingError, IdentifiedGroup]] =
    Future.successful(Right(IdentifiedGroup("1", grouped)))

  def identifiedGroup(
      uuid: String
  ): Future[Either[GroupingError, Option[IdentifiedGroup]]] = ???
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
  import scala.concurrent.ExecutionContext.Implicits.global

  def grouping(
      candidates: Candidates
  ): Future[Either[GroupingError, IdentifiedGroup]] =
    (for {
      grouped    <- EitherT(groupingRepository.grouping(candidates))
      identified <- EitherT(groupingRepository.generateIdentity(grouped))
    } yield identified).value
  def identifiedGroup(
      uuid: String
  ): Future[Either[GroupingError, Option[IdentifiedGroup]]] =
    groupingRepository.identifiedGroup(uuid)

}

object GroupingUseCase extends GroupingUseCase with MixinGroupingRepositoryImpl
