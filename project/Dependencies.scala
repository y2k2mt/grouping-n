import sbt._
import Keys._

object Dependencies {

  val CatsVersion       = "2.6.1"
  val CatsEffectVersion      = "3.2.9"
  val Http4sVersion     = "0.23.4"
  val Fs2Version        = "3.1.2"
  val DoobieVersion     = "1.0.0-RC1"
  val PostgresVersion   = "42.2.5"
  val FlywayVersion     = "7.5.4"
  val CirceVersion      = "0.14.1"
  val SloggingVersion   = "3.9.4"
  val Log4jVersion      = "2.14.0"
  val JacksonVersion    = "2.12.1"
  val DisruptorVersion  = "3.4.2"
  val ScalaTestVersion  = "3.2.10"

  val akkaHttpVersion   = "10.2.4"
  val akkaVersion       = "2.6.13"
  val pureConfigVersion = "0.14.1"

  // For appendix
  //val ZioVersion       = "1.0.4-2"

  object Compile {
    val cats = "org.typelevel" %% "cats-core" % CatsVersion
    val catsEffect = "org.typelevel" %% "cats-effect" % CatsEffectVersion
    val http4sBlazeServer =
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion
    val http4sBlazeClient =
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion
    val http4sCirce    = "org.http4s"    %% "http4s-circe"    % Http4sVersion
    val http4sDsl      = "org.http4s"    %% "http4s-dsl"      % Http4sVersion
    val fs2Core        = "co.fs2"        %% "fs2-core"        % Fs2Version
    val fs2IO          = "co.fs2"        %% "fs2-io"          % Fs2Version
    val doobie         = "org.tpolecat"  %% "doobie-core"     % DoobieVersion
    val doobieHikari   = "org.tpolecat"  %% "doobie-hikari"   % DoobieVersion
    val doobiePostgres = "org.tpolecat"  %% "doobie-postgres" % DoobieVersion
    val postgres       = "org.postgresql" % "postgresql"      % PostgresVersion
    val flyway         = "org.flywaydb"   % "flyway-core"     % FlywayVersion
    val circe          = "io.circe"      %% "circe-generic"   % CirceVersion
    val circeParser    = "io.circe"      %% "circe-parser"    % CirceVersion

    val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    val akkaHttpSpray =
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
    val akkaActorTyped = "com.typesafe.akka"     %% "akka-actor-typed" % akkaVersion
    val akkaStream     = "com.typesafe.akka"     %% "akka-stream"      % akkaVersion
    val pureConfig     = "com.github.pureconfig" %% "pureconfig"       % pureConfigVersion
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
    doobie,
    doobiePostgres,
    doobieHikari,
    circe,
    circeParser
  )

  val cake = lib ++= rootLibs ++ Seq(
    cats,
    akkaHttp,
    akkaHttpSpray,
    akkaActorTyped,
    akkaStream,
    pureConfig
  )

}
