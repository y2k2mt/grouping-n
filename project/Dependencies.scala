import sbt._
import Keys._

object Dependencies {

  val CatsVersion      = "2.4.2"
  val Http4sVersion    = "0.21.0"
  val Fs2Version       = "2.5.3"
  val MonixVersion     = "3.3.0"
  val DoobieVersion    = "0.10.0"
  val PostgresVersion  = "42.2.5"
  val FlywayVersion    = "7.5.4"
  val CirceVersion     = "0.13.0"
  val SloggingVersion  = "3.9.2"
  val Log4jVersion     = "2.14.0"
  val JacksonVersion   = "2.12.1"
  val DisruptorVersion = "3.4.2"
  val ScalaTestVersion = "3.2.2"
  // For appendix
  //val ZioVersion       = "1.0.4-2"

  object Compile {
    val cats = "org.typelevel" %% "cats-core" % CatsVersion
    val http4sBlazeServer =
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion
    val http4sBlazeClient =
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion
    val http4sCirce    = "org.http4s"    %% "http4s-circe"    % Http4sVersion
    val http4sDsl      = "org.http4s"    %% "http4s-dsl"      % Http4sVersion
    val fs2Core        = "co.fs2"        %% "fs2-core"        % Fs2Version
    val fs2IO          = "co.fs2"        %% "fs2-io"          % Fs2Version
    val monix          = "io.monix"      %% "monix"           % MonixVersion
    val doobie         = "org.tpolecat"  %% "doobie-core"     % DoobieVersion
    val doobieHikari   = "org.tpolecat"  %% "doobie-hikari"   % DoobieVersion
    val doobiePostgres = "org.tpolecat"  %% "doobie-postgres" % DoobieVersion
    val postgres       = "org.postgresql" % "postgresql"      % PostgresVersion
    val flyway         = "org.flywaydb"   % "flyway-core"     % FlywayVersion
    val circe          = "io.circe"      %% "circe-generic"   % CirceVersion
    val circeParser    = "io.circe"      %% "circe-parser"    % CirceVersion
    val scalaLogging =
      "com.typesafe.scala-logging" %% "scala-logging" % SloggingVersion
    val slf4j     = "org.apache.logging.log4j" % "log4j-slf4j-impl" % Log4jVersion
    val log4j     = "org.apache.logging.log4j" % "log4j-api"        % Log4jVersion
    val log4jCore = "org.apache.logging.log4j" % "log4j-core"       % Log4jVersion
    val jackson =
      "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion
    // In-memory queue for async logger
    val disruptor = "com.lmax" % "disruptor" % DisruptorVersion
    // For appendix
    //val zio         = "dev.zio"      %% "zio"           % ZioVersion
    //val zioMacros   = "dev.zio"      %% "zio-macros"    % ZioVersion
    object Test {
      val scalaTest = "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
    }
  }

  import Compile._
  import Test._

  val lib = libraryDependencies

  val rootLibs = Seq(
    postgres,
    flyway,
    scalaLogging,
    slf4j,
    log4j,
    log4jCore,
    jackson,
    disruptor,
    scalaTest
  )

  val tagless = lib ++= rootLibs ++ Seq(
    cats,
    http4sBlazeServer,
    http4sBlazeClient,
    http4sCirce,
    http4sDsl,
    fs2IO,
    fs2Core,
    monix,
    doobie,
    doobiePostgres,
    doobieHikari,
    circe,
    circeParser
  )
}
