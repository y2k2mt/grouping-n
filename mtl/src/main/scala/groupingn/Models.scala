package groupingn.models

import cats.data.EitherT
import cats.effect.Async
import cats.Monad
import cats.mtl.Raise

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

trait GroupingAlgebra {
  def grouping[F[_]: Async](
      candidates: Candidates
  )(implicit F: Raise[F, GroupingError]): F[Grouped]
  def generateIdentity[F[_]: Async](
      grouped: Grouped
  )(implicit F: Raise[F, GroupingError]): F[IdentifiedGroup]
  def identifiedGroup[F[_]: Async](
      uuid: String
  )(implicit F: Raise[F, GroupingError]): F[Option[IdentifiedGroup]]
}
