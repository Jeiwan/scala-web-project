name := """scala-web-project"""
version := "1.0-SNAPSHOT"
scalaVersion := "2.11.8"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
pipelineStages := Seq(digest)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  "com.softwaremill.macwire" %% "macros" % "2.2.0" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.2.0",
  "org.postgresql" % "postgresql" % "42.0.0",
  "org.scalikejdbc" %% "scalikejdbc" % "2.5.2",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.5.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "de.svenkubiak" % "jBCrypt" % "0.4.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0",
  "org.mockito" % "mockito-core" % "2.7.22"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
