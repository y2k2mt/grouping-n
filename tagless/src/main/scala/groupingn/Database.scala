package groupingn

import cats.effect._
import monix.eval.Task
import doobie._
import doobie.hikari._
import org.flywaydb.core.Flyway

object Database {

  lazy val transactor: Resource[Task, HikariTransactor[Task]] = {
    val connectionURL = new java.net.URI(
      sys.env
        .get("DB_URL")
        .getOrElse("postgresql://postgres@localhost:5432/postgres")
    )
    val jdbcUrl =
      s"jdbc:${connectionURL.getScheme}://${connectionURL.getHost}:${connectionURL.getPort}${connectionURL.getPath}"
    val userInfo = Option(connectionURL.getUserInfo).map(_.split(":"))
    val userName = userInfo.map(_.lift(0).getOrElse("")).getOrElse("")
    val password = userInfo.map(_.lift(1).getOrElse("")).getOrElse("")

    for {
      ce <- ExecutionContexts.fixedThreadPool[Task](10)
      be <- Blocker[Task]
      xa <- HikariTransactor.newHikariTransactor[Task](
        "org.postgresql.Driver",
        jdbcUrl,
        userName,
        password,
        ce,
        be
      )
    } yield xa
  }

  def initialize: Task[Unit] = {
    Database.transactor.use {
      _.configure { dataSource =>
        Task {
          val flyWay = Flyway.configure().dataSource(dataSource).load()
          flyWay.migrate()
          ()
        }
      }
    }
  }
}
