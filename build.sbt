name := "template-scala-parallel-ecommercerecommendation"

parallelExecution in Test := false
test in assembly := {}

scalaVersion := "2.11.8"
libraryDependencies ++= Seq(
  "org.apache.predictionio" %% "apache-predictionio-core" % "0.12.0-incubating" % "provided",
  "org.apache.spark"        %% "spark-mllib"              % "2.1.1" % "provided",
  "org.scalatest"           %% "scalatest"                % "3.0.4" % "test")
