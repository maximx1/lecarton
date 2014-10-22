name := """lecarton"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.mongodb" %% "casbah" % "2.5.0"
)

instrumentSettings

ScoverageKeys.minimumCoverage := 95

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.excludedPackages in ScoverageCompile := "<empty>;controllers.*;views.*"