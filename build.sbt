ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.4"

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all"
)

Compile / doc / target := baseDirectory.value / "docs" / "api"

lazy val root = (project in file("."))
  .settings(
    name := "scryption",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.19.0" % Test
    ),
    scalafmtOnCompile := true
  )

addCommandAlias(
  "checkAll",
  ";scalafmtAll;scalafmtSbt;scalafix --check;test"
)
