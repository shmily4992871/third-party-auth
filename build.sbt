name := "third-party-auth"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

val reactiveMongoVer = "0.16.0-play26"

scalafmtConfig := file(".scalafmt.conf")
scalafmtOnCompile := true
scalafmtTestOnCompile := true
scalafmtVersion := "1.3.0"

lazy val versions = new {
  val reactiveMongo = "0.16.0-play26"
  val scalatestplus = "4.0.2"
  val swagger       = "1.6.0"
  val swaggerUi     = "3.2.2"
  val jwtcore       = "0.16.0"
  val json4s        = "3.4.2"
  val twitterUtil   = "18.10.0"
  val twitterInject = "18.1.0"
  val hamsters      = "2.3.0"
  val errors        = "1.2"
}

libraryDependencies ++= Seq(
  guice,
  "org.reactivemongo"            %% "play2-reactivemongo" % versions.reactiveMongo,
  "io.swagger"                   %% "swagger-play2"       % versions.swagger,
  "org.webjars"                  % "swagger-ui"           % versions.swaggerUi,
  "org.json4s"                   %% "json4s-jackson"      % versions.json4s,
  "org.scalatestplus.play"       %% "scalatestplus-play"  % versions.scalatestplus % Test,
  "io.github.scala-hamsters"     %% "hamsters"            % versions.hamsters,
  "com.pauldijou"                %% "jwt-core"            % versions.jwtcore,
  "com.twitter"                  %% "util-core"           % versions.twitterUtil,
  "com.twitter"                  %% "inject-core"         % versions.twitterInject,
  "com.github.mehmetakiftutuncu" %% "errors"              % versions.errors
)

import play.sbt.routes.RoutesKeys

RoutesKeys.routesImport += "play.modules.reactivemongo.PathBindables._"
