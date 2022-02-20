package groupingn

import cats.effect.*
import models.interpretors.implicits.*

object Main extends IOApp {

  type Effect[T] = IO[T]

  def run(args: List[String]) = {
    for {
      _ <- Database.initialize[Effect].to[Effect]
      s <- Server.stream[Effect].compile.drain.as(ExitCode.Success).to[Effect]
    } yield s
  }
}
