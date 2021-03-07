package groupingn

import cats.Monad
import models._

trait GroupingAction[F[_]] {
  def grouping(c: Candidates): F[Either[GroupingError, IdentifiedGroup]]
  def identifiedGroup(
      id: String
  ): F[Either[GroupingError, Option[IdentifiedGroup]]]
}

object GroupingAction {
  implicit def apply[F[_]](implicit ev: GroupingAction[F]): GroupingAction[F] =
    ev

  def impl[F[_]: Monad](implicit alg: GroupingAlgebra[F]): GroupingAction[F] =
    new GroupingAction[F] {

      def grouping(c: Candidates): F[Either[GroupingError, IdentifiedGroup]] =
        GroupingUseCase.grouping[F](c)

      def identifiedGroup(
          id: String
      ): F[Either[GroupingError, Option[IdentifiedGroup]]] =
        GroupingUseCase.identifiedGroup[F](id)
    }
}
