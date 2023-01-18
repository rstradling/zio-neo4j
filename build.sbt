val scala2Version = "2.13.10"
val scala3Version = "3.2.1"
val zioVersion = "2.0.5"
val neo4jDriverVersion = "5.4.0"

ThisBuild / organization := "com.stradsoftware"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3Version
ThisBuild / crossScalaVersions := Seq(scala2Version, scala3Version)

lazy val root = (project in file("."))
    .settings(
        scalacOptions ++= Seq(
        "-deprecation",
        "-Xfatal-warnings")
        )
    .dependsOn(neo4j)
    .aggregate(neo4j)

lazy val neo4j = (project in file("./neo4j"))
    .settings(
        name := "zio-neo4j",
        libraryDependencies ++= Seq(
         "dev.zio" %% "zio" % zioVersion,
         "dev.zio" %% "zio-streams" % zioVersion,
         "org.neo4j.driver" % "neo4j-java-driver" % neo4jDriverVersion,
         "dev.zio" %% "zio-test" % zioVersion % "test",
         "dev.zio" %% "zio-test-sbt" % zioVersion % "test",
         "dev.zio" %% "zio-test-magnolia" % zioVersion % "test",
        ),
        testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )
