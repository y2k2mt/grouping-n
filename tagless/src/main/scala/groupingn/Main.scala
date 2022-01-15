package groupingn

import cats.effect._
import models.interpretors.implicits._

object Main extends IOApp {

  def run(args: List[String]) = {
    for {
      _ <- Database.initialize[IO].to[IO]
      s <- Server.stream[IO].compile.drain.as(ExitCode.Success).to[IO]
    } yield s
  }
}
