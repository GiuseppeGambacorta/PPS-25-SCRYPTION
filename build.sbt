ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.8"

lazy val root = (project in file("."))
  .settings(
    name := "scryption",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test,
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    scalafmtOnCompile := true
  )
