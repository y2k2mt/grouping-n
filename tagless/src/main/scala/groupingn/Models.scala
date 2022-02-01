package groupingn.models

import cats.data.EitherT
import cats.effect.Async

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
  )(implicit F: Async[F]): F[Either[GroupingError, Grouped]]
  def generateIdentity[F[_]: Async](
      grouped: Grouped
  ): F[Either[GroupingError, IdentifiedGroup]]
  def identifiedGroup[F[_]](
      uuid: String
  )(implicit F: Async[F]): F[Either[GroupingError, Option[IdentifiedGroup]]]
}

object GroupingUseCase {

  def grouping[F[_]: Async](
      candidates: Candidates
  )(implicit
      alg: GroupingAlgebra
  ): F[Either[GroupingError, IdentifiedGroup]] =
    (for {
      grouped    <- EitherT(alg.grouping[F](candidates))
      identified <- EitherT(alg.generateIdentity[F](grouped))
    } yield identified).value

  def identifiedGroup[F[_]: Async](
      id: String
  )(implicit
      alg: GroupingAlgebra
  ): F[Either[GroupingError, Option[IdentifiedGroup]]] =
    alg.identifiedGroup[F](id)

}
