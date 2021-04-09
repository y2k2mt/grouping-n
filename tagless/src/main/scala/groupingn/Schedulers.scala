package groupingn

import monix.execution.Scheduler

object Schedulers {

  object Implicits {
    implicit val computation: Scheduler =
      Scheduler.computation()
  }
  val fixedPool: Scheduler =
    Scheduler.fixedPool(name = "blocking-io", poolSize = 10)

}
