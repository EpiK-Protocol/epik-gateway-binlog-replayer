import Commons._

organization := "com.epik"

// nebula version we use.
val nebulaClientVersion = "2.0.0-SNAPSHOT"
// spark & hadoop version we use.
scalaVersion := "2.12.8"
val sparkVersion  = "3.0.1"
val hadoopVersion = "3.2.1"

name := "epik-gateway-binlog-replayer"
version := "1.0.0-SNAPSHOT"

test in assembly in ThisBuild := {}

//not include src and doc when dist
sources in (Compile, doc) in ThisBuild := Seq.empty
publishArtifact in (Compile, packageDoc) in ThisBuild := false
sources in (packageSrc) := Seq.empty

resolvers += Resolver.mavenLocal

libraryDependencies ++= commonDependencies
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % s"${sparkVersion}" % "provided"
    exclude ("org.apache.hadoop", "hadoop-client"),
  "org.apache.spark"  %% "spark-hive"          % s"${sparkVersion}" % "provided",
  "org.apache.hadoop" % "hadoop-common"        % s"${hadoopVersion}" % "provided",
  "org.apache.hadoop" % "hadoop-client"        % s"${hadoopVersion}" % "provided",
  "com.vesoft"        % "client"               % nebulaClientVersion,
  "mysql"             % "mysql-connector-java" % "5.1.38" % Runtime,
  //cmd line parsing
  "commons-cli"   % "commons-cli" % "1.4",
  "commons-io"    % "commons-io"  % "2.8.0",
  "org.scalatest" %% "scalatest"  % "3.0.4" % Test
)

//CAUTION: when dependency with version of X-SNAPSHOT is updated, you should comment out the following line, and run sbt update
updateOptions := updateOptions.value.withLatestSnapshots(false)

assemblyMergeStrategy in assembly := {
  case PathList("org", "slf4j", xs @ _*)                            => MergeStrategy.discard
  case PathList("org", "apache", "logging", _)                      => MergeStrategy.discard
  case PathList("ch", "qos", "logback", _)                          => MergeStrategy.discard
  case PathList("org", "scalactic", _)                              => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith "axiom.xml"            => MergeStrategy.filterDistinctLines
  case PathList(ps @ _*) if ps.last endsWith "Log$Logger.class"     => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "ILoggerFactory.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyShadeRules in assembly := Seq(
  ShadeRule
    .rename("org.apache.commons.cli.**" -> "shadecli.org.apache.commons.cli.@1")
    .inAll
)

assemblyJarName in assembly := "epik-gateway-binlog-replayer.jar"

test in assembly := {}
// should not include scala runtime when submitting spark job
assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(includeScala = false)
