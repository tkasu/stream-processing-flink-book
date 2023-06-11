val flinkVersion = "1.17.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "stream-processing-flink-book",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.3.0",
    libraryDependencies ++= List(
      "org.apache.flink" % "flink-streaming-java" % flinkVersion % "provided",
      "org.apache.flink" % "flink-table-common" % flinkVersion % "provided",
      "org.apache.flink" % "flink-table-api-java" % flinkVersion % "provided",
      "org.apache.flink" % "flink-table-api-java-bridge" % flinkVersion % "provided",
      "org.apache.flink" % "flink-table-planner-loader" % flinkVersion % "provided",
      "org.apache.flink" % "flink-table-runtime" % flinkVersion % "provided",
      "org.apache.flink" % "flink-runtime-web" % flinkVersion % "provided",
      "org.apache.flink" % "flink-json" % flinkVersion % "provided",
      "org.apache.flink" % "flink-statebackend-rocksdb" % flinkVersion % "provided",
      "org.apache.flink" % "flink-state-processor-api" % flinkVersion % "provided",
      "org.apache.flink" % "flink-connector-kafka" % flinkVersion % "provided"
    )
  )

run in Compile := Defaults
  .runTask(
    fullClasspath in Compile,
    mainClass in (Compile, run),
    runner in (Compile, run)
  )
  .evaluated
runMain in Compile := Defaults
  .runMainTask(fullClasspath in Compile, runner in (Compile, run))
  .evaluated

javacOptions ++= Seq("-source", "11", "-target", "11")
