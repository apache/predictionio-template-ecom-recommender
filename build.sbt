import AssemblyKeys._

assemblySettings

name := "template-scala-parallel-ecommercerecommendation"

organization := "io.prediction"

libraryDependencies ++= Seq(
  "io.prediction"    %% "core"          % pioVersion.value  % "provided",
  "org.apache.spark" %% "spark-core"    % "1.3.0" % "provided",
  "org.apache.spark" %% "spark-mllib"   % "1.3.0" % "provided",
  "org.scalatest"    %% "scalatest"     % "2.2.1" % "test")
