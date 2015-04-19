resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.0.4")

offline := true