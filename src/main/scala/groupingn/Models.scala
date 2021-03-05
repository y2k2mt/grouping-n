package groupingn.models

import cats.Monad
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

trait GroupingAlgebra[F[_]] {
  def grouping(candidates: Candidates): F[Either[GroupingError, Grouped]]
  def generateIdentity(
      grouped: Grouped
  ): F[Either[GroupingError, IdentifiedGroup]]
  def identifiedGroup(
      uuid: String
  ): F[Either[GroupingError, Option[IdentifiedGroup]]]
}

object GroupingUseCase {

  def grouping[F[_]: Monad](
      candidates: Candidates
  )(implicit
      alg: GroupingAlgebra[F]
  ): F[Either[GroupingError, IdentifiedGroup]] =
    (for {
      grouped    <- EitherT(alg.grouping(candidates))
      identified <- EitherT(alg.generateIdentity(grouped))
    } yield identified).value

  def identifiedGroup[F[_]](
      id: String
  )(implicit
      alg: GroupingAlgebra[F]
  ): F[Either[GroupingError, Option[IdentifiedGroup]]] = alg.identifiedGroup(id)

}
