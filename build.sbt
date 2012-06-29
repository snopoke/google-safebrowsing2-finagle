import com.twitter.sbt._

seq((
  Project.defaultSettings ++
  StandardProject.newSettings
): _*)

organization := "com.github.snopoke"

name := "safebrowsing"

version := "0.1.0"

libraryDependencies ++= Seq(
  "com.github.snopoke" %% "safebrowsing2" % "0.1.0",
  "com.twitter" %% "finagle-core" % "2.0.1",
  "com.twitter" %% "finagle-http" % "2.0.1",
  "com.twitter" %% "finagle-ostrich4" % "2.0.1",
  "org.scalatest" %% "scalatest" % "1.7.1" % "test",
  "org.mockito" % "mockito-core" % "1.9.0" % "test",
  "junit" % "junit" % "4.10" % "test",
  "com.twitter" %% "scalatest-mixins" % "1.0.0" % "test"
)

mainClass in (Compile, run) := Some("com.github.snopoke.safebrowsing2.Main")

mainClass in (Compile, packageBin) := Some("com.github.snopoke.safebrowsing2.Main")

scalacOptions ++= Seq("-unchecked", "-deprecation")

parallelExecution in Test := false