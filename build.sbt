ThisBuild / scalaVersion     := "2.13.6"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "actorsregistry",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.8",
      "dev.zio" %% "zio-test" % "1.0.8" % Test
    ),
    libraryDependencies += "io.d11" %% "zhttp" % "1.0.0.0-RC16",
    libraryDependencies += "dev.zio" %% "zio-actors" % "0.0.9",
    libraryDependencies += "dev.zio" %% "zio-macros" % "1.0.8",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
