package groupingn

import cats.effect.*
import models.interpretors.implicits.*

object Main extends IOApp {

  def run(args: List[String]) = {
    for {
      _ <- Database.initialize[IO].to[IO]
      s <- Server.stream[IO].compile.drain.as(ExitCode.Success).to[IO]
    } yield s
  }
}
