package groupingn

import cats.*
import cats.effect.*
import models.*
import cats.mtl.*
import cats.mtl.implicits.*
import cats.syntax.all.*

object GroupingAction {

  def grouping[F[_]: Async](
      candidates: Candidates
  )(implicit
      F: Raise[F, GroupingError],
      A: Ask[F, GroupingAlgebra]
  ): F[IdentifiedGroup] =
    for {
      alg        <- A.ask
      grouped    <- alg.grouping[F](candidates)
      identified <- alg.generateIdentity[F](grouped)
    } yield identified

  def identifiedGroup[F[_]: Async](
      id: String
  )(implicit
      F: Raise[F, GroupingError],
      A: Ask[F, GroupingAlgebra]
  ): F[Option[IdentifiedGroup]] =
    for {
      alg    <- A.ask
      result <- alg.identifiedGroup[F](id)
    } yield result
}
