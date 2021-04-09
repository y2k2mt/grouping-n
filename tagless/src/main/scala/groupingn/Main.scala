package groupingn

import cats.effect.{ExitCode, IO, IOApp}
import monix.eval.Task
import monix.eval.instances.CatsConcurrentEffectForTask
import models.interpretors.implicits._
import Schedulers.Implicits.computation

object Main extends IOApp {
  implicit val taskOptions = Task.defaultOptions
  implicit val monixEffect = new CatsConcurrentEffectForTask()

  def run(args: List[String]) = {
    for {
      _ <- Database.initialize.to[IO]
      s <- Server.stream[Task].compile.drain.as(ExitCode.Success).to[IO]
    } yield s
  }
}
