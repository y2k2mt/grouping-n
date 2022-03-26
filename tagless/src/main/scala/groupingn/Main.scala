package groupingn

import cats.effect.*
import models.interpretors.implicits.*

object Main extends IOApp {

  type F[T] = IO[T]

  def run(args: List[String]) = {
    for {
      _ <- Database.initialize[F]
      s <- Server.stream[F].compile.drain.as(ExitCode.Success)
    } yield s
  }
}
