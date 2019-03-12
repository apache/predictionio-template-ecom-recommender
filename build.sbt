name := "template-scala-parallel-ecommercerecommendation"

parallelExecution in Test := false
test in assembly := {}

scalaVersion := "2.11.12"
libraryDependencies ++= Seq(
  "org.apache.predictionio" %% "apache-predictionio-core" % "0.14.0" % "provided",
  "org.apache.spark"        %% "spark-mllib"              % "2.4.0" % "provided",
  "org.scalatest"           %% "scalatest"                % "3.0.5" % "test")
