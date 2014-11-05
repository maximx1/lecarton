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
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.mindrot" % "jbcrypt" % "0.3m"
)

libraryDependencies += "org.mongodb" %% "casbah" % "2.7.3"

instrumentSettings

ScoverageKeys.minimumCoverage := 95

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.excludedPackages in ScoverageCompile := "<empty>;controllers.*;views.*"