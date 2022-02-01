package groupingn

import cats.effect.*
import doobie.*
import doobie.hikari.*
import org.flywaydb.core.Flyway

object Database {

  def transactor[F[_]: Async]: Resource[F, HikariTransactor[F]] = {

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
      ce <- ExecutionContexts.fixedThreadPool[F](10)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        jdbcUrl,
        userName,
        password,
        ce
      )
    } yield xa
  }

  def initialize[F[_]](implicit F: Async[F]) =
    Database.transactor[F].use { xa =>
      xa.configure { dataSource =>
        F.delay {
          val flyWay = Flyway.configure().dataSource(dataSource).load()
          flyWay.migrate()
        }
      }
    }
}
