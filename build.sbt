import sbtassembly.AssemblyPlugin.autoImport.*
import sbtassembly.MergeStrategy

ThisBuild / version := "1.0"

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
      "com.lihaoyi" %% "upickle" % "4.4.3",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.19.0" % Test
    ),
    scalafmtOnCompile := true,
    assembly / mainClass := Some("org.scryption.MainWindow"),
    assembly / assemblyJarName := s"${name.value}-${version.value}-assembly.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("META-INF", xs @ _*)
          if xs.exists(_.toLowerCase.endsWith(".sf")) || xs.exists(_.toLowerCase.endsWith(".dsa")) || xs
            .exists(_.toLowerCase.endsWith(".rsa")) =>
        MergeStrategy.discard
      case PathList("META-INF", xs @ _*) if xs.exists(_.equalsIgnoreCase("index.list")) =>
        MergeStrategy.discard
      case PathList("META-INF", xs @ _*) if xs.exists(_.equalsIgnoreCase("dependencies")) =>
        MergeStrategy.discard
      case "module-info.class" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )

addCommandAlias(
  "checkAll",
  ";scalafmtAll;scalafmtSbt;scalafix --check;test"
)
