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
  "com.github.tototoshi" %% "play-json4s-test-jackson" % "0.3.1" % "test"
)

instrumentSettings

ScoverageKeys.minimumCoverage := 90

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.excludedPackages in ScoverageCompile := "<empty>;controllers.*;views.*"

parallelExecution in Test := false

testOptions in Test += Tests.Argument("-oF")