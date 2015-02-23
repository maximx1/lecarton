name := """mongotoh2"""

version := "1.0"

scalaVersion := "2.11.5"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.3.6",
  "com.h2database" % "h2" % "1.3.166",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.mongodb" %% "casbah" % "2.7.3"
)