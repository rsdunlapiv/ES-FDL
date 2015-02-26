name := """es-fdl"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

libraryDependencies += "net.sourceforge.owlapi" % "owlapi-distribution" % "3.4.10"

libraryDependencies += "com.hermit-reasoner" % "org.semanticweb.hermit" % "1.3.8.4"

