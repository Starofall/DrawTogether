// support for android in sbt
addSbtPlugin("org.scala-android" % "sbt-android" % "1.6.3")
// support hotdeploy on android
// addSbtPlugin("org.scala-android" % "sbt-android-protify" % "1.2.3")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.1")

// addSbtPlugin("com.jamesward" % "play-auto-refresh" % "0.0.14")

libraryDependencies += "net.sf.proguard" % "proguard-base" % "5.2.1"

// deployment
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.2")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")


