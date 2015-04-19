name := """lecarton"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

scalacOptions += "-target:jvm-1.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.pegdown" % "pegdown" % "1.2.1",
  "com.github.tototoshi" %% "play-json4s-jackson" % "0.3.1",
  "com.github.tototoshi" %% "play-json4s-test-jackson" % "0.3.1" % "test",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick" % "0.8.1",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
)

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 90

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>;controllers.*;views.*;models.*"

parallelExecution in Test := true