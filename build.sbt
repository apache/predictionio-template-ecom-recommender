import AssemblyKeys._

assemblySettings

name := "template-scala-parallel-ecommercerecommendation"

organization := "org.apache.predictionio"

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.apache.predictionio" %% "apache-predictionio-core" % "0.10.0-incubating" % "provided",
  "org.apache.spark"        %% "spark-core"               % "1.3.0" % "provided",
  "org.apache.spark"        %% "spark-mllib"              % "1.3.0" % "provided",
  "org.scalatest"           %% "scalatest"                % "2.2.1" % "test")
