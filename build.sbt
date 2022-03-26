import sbtassembly.AssemblyPlugin._
import com.typesafe.sbt.packager.archetypes._

val projectName  = "groupingn"
val buildVersion = "0.1.0"

val scalaOptions = Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Ywarn-unused",
  "-deprecation",
  "-unchecked",
  "-Xlint:_,-byname-implicit", // enable handy linter warnings without byname implicit https://github.com/scala/bug/issues/12072
  "-Ydelambdafy:method",
  "-Ymacro-annotations",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Yrangepos" // required by SemanticDB compiler plugin
)

val scala3Options = Seq(
  "-feature",
  "-unchecked",
  "-encoding",
  "utf8",
  "-deprecation",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ykind-projector:underscores",
  "-source:future"
)

val jOptions = Seq(
  "-server",
  "-Xms64m",
  "-Xmx64m",
  "-XX:MaxMetaspaceSize=128m",
  "-XX:ReservedCodeCacheSize=128m",
  "-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
) ++ {
  val enableTrace = sys.props.get("tracelog").exists(_.toBoolean)
  if (enableTrace) {
    Seq(
      "-Dapp.log4j.loglevel.root=trace",
      "-Dapp.log4j.loglevel.extlib=debug",
      "-Dapp.log4j.tracelog.ref.prefix=ENABLE"
    )
  } else {
    Seq(
      "-Dapp.log4j.loglevel.root=info",
      "-Dapp.log4j.loglevel.extlib=info",
      "-Dapp.log4j.tracelog.ref.prefix=DISABLE"
    )
  }
}

val asmSettings = assemblySettings ++ Seq(
  assemblyJarName := s"${projectName}-${buildVersion}.jar",
  assembly / assemblyMergeStrategy := {
    case PathList(ps @ _*) if ps.last endsWith ".properties" =>
      MergeStrategy.first
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

val scalaFixSettings = Seq(
  addCompilerPlugin(scalafixSemanticdb),           // enable SemanticDB
  semanticdbEnabled := true,                       // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision // use Scalafix compatible version
)

val rootSettings = Seq(
  organization := "com.github.y2k2mt",
  version := buildVersion,
  name := projectName,
  scalaVersion := "2.13.7",
  scalacOptions := scalaOptions,
  run / javaOptions ++= jOptions,
  reStart / javaOptions ++= jOptions,
  Test / javaOptions ++= jOptions,
  Test / fork := true,
  artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
    artifact.name + "-" + module.revision + "." + artifact.extension
  }
) ++ asmSettings

lazy val root = (project in file("."))
  .settings(rootSettings: _*)
  .enablePlugins(JavaAppPackaging)

lazy val tagless = (project in file("tagless"))
  .settings(rootSettings: _*)
  .settings(
    name := s"${projectName}-tagless",
    scalaVersion := "3.1.1",
    scalacOptions := scala3Options,
    Dependencies.tagless
  )
  .enablePlugins(JavaAppPackaging)

lazy val cake = (project in file("cake"))
  .settings(rootSettings: _*)
  .settings(
    name := s"${projectName}-cake",
    Dependencies.cake,
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )
  .enablePlugins(JavaAppPackaging)

Revolver.settings
run / fork := true
