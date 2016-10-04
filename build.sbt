name := "DrawTogether"

import sbt._
import sbt.Keys._
import android.Keys._
import com.typesafe.sbt.SbtNativePackager.packageArchetype

/** settings for client and server */
lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "DrawTogehter",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-encoding", "utf8"
  ),
  resolvers += "jitpack" at "https://jitpack.io",
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  resolvers += Resolver.jcenterRepo,
  // offer optimization on hot code areas
  libraryDependencies += "com.nativelibs4java" %% "scalaxy-streams" % "0.3.4" % "provided"
)

/** the shared project containing code used by server and android */
lazy val shared = Project(id = "shared", base = file("shared"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq("com.github.benhutchison" %% "prickle" % "1.1.10"),
    // export Jars is needed for android
    exportJars := true
  )

lazy val androidScala = Project(id = "android", base = file("android"))
  .settings(commonSettings: _*)
  .settings(android.Plugin.androidBuild: _*)
  //  .settings(protifySettings: _*)
  .settings(
  platformTarget in Android := "android-23",
  // settings for proguard
  useProguard in Android := true,
  useProguardInDebug in Android := true,
  proguardScala in Android := true,
  proguardOptions in Android ++= Seq(
    //      "-dontobfuscate",
    //      "-dontoptimize",
    "-keep class scala.Dynamic",
    "-keepattributes Signature",
    "-dontwarn scala.collection.**",
    "-dontwarn org.scaloid.**",
    "-keep class android.support.v7.app.AlertDialog { *; }",
    "-keep class android.support.v7.app.AlertDialog$Builder { *; }"
  ),
  proguardCache in Android ++= Seq(// define modules that should be cached
    "android.support",
    "android.tools",
    "java-websocket",
    "github.benhutchison",
    "org.scaloid"
  ),
  packagingOptions in Android := PackagingOptions(excludes = Seq(
    "META-INF/LICENSE.txt",
    "META-INF/NOTICE.txt")),
  updateCheck in Android := {}, // disable update check
  typedResourcesIgnores ++= Seq(// ignore these for typed ressources, as they create a namespace collision
    "petrov.kristiyan.colorpicker",
    "colorpicker-library:1.1.0",
    "colorpicker-library",
    "com.flask.colorpicker"
  ),
  libraryDependencies ++= Seq(
    // Scala Dependencies
    "com.github.benhutchison" %% "prickle" % "1.1.10", // serialization
    "org.scaloid" %% "scaloid" % "4.2", // syntactic sugar
    "org.java-websocket" % "Java-WebSocket" % "1.3.0", // websocket
    "com.android.support" % "support-annotations" % "23.4.0",
    // Android Dependencies (starting with arr)
    aar("com.android.support" % "support-v4" % "23.4.0"),
    aar("com.android.support" % "design" % "23.4.0"),
    aar("com.android.support" % "appcompat-v7" % "23.4.0"),
    aar("com.android.support" % "cardview-v7" % "23.4.0"),
    aar("com.android.support" % "recyclerview-v7" % "23.4.0"),
    aar("com.github.QuadFlask" % "colorpicker" % "0.0.10")
  ),
  // set javac version to java 7
  javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
  // forward commands to android project
  run <<= run in Android,
  install <<= install in Android
)
  .dependsOn(shared)

/** play server */
lazy val server = Project(id = "server", base = file("server"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      // network serialization
      "com.github.benhutchison" %% "prickle" % "1.1.10",
      // slick database abstraction
      "com.typesafe.play" %% "play-slick" % "2.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1",
      "com.typesafe.slick" %% "slick-codegen" % "3.1.1",
      // h2 database access
      "com.h2database" % "h2" % "1.4.191",
      // used for password bcrypting
      "org.mindrot" % "jbcrypt" % "0.3m"
    ),
    slick <<= slickCodeGenTask // register manual sbt command
  )
  .settings(
    dockerfile in docker := {
      val appDir = stage.value
      val targetDir = "/app"
      new Dockerfile {
        from("java")
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
        copy(appDir, targetDir)
        expose(10010)
      }
    },
    buildOptions in docker := BuildOptions(cache = false)
  )
  // enable play framework
  .enablePlugins(PlayScala)
  .enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)
  .dependsOn(shared)

// code generation task
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val outputDir = new File("./server/app/").getPath // place generated files in sbt's managed sources folder
val url = "jdbc:h2:file:./storage/database" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
val jdbcDriver = "org.h2.Driver"
  val slickDriver = "slick.driver.H2Driver"
  val pkg = "models"
  toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg), s.log))
  val fname = outputDir + "/Tables.scala"
  Seq(file(fname))
}