import com.twitter.sbt._

seq((
  Project.defaultSettings ++
  StandardProject.newSettings
): _*)

resolvers ++= Seq(Resolver.defaultLocal, Resolver.mavenLocal)

organization := "com.github.snopoke"

name := "safebrowsing2.finagle"

version := "0.1.0"

libraryDependencies ++= Seq(
  "com.github.snopoke" %% "safebrowsing2" % "0.1.0",
  "com.twitter" %% "finagle-core" % "2.0.1",
  "com.twitter" %% "finagle-http" % "2.0.1",
  "com.twitter" %% "finagle-ostrich4" % "2.0.1",
  "org.hsqldb" % "hsqldb" % "2.2.8",
  "org.slf4j" % "slf4j-jcl" % "1.6.6",
  "mysql" % "mysql-connector-java" % "5.1.20",
  "org.scalatest" %% "scalatest" % "1.7.1" % "test",
  "org.mockito" % "mockito-core" % "1.9.0" % "test",
  "junit" % "junit" % "4.10" % "test",
  "com.twitter" %% "scalatest-mixins" % "1.0.0" % "test"
)

mainClass in (Compile, run) := Some("com.github.snopoke.safebrowsing2.finagle.Main")

mainClass in (Compile, packageBin) := Some("com.github.snopoke.safebrowsing2.finagle.Main")

scalacOptions ++= Seq("-unchecked", "-deprecation")

parallelExecution in Test := false