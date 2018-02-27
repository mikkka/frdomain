import sbt.Keys.libraryDependencies

val catsVersion = "1.0.0"
val catsAll = Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-macros" % catsVersion,
  "org.typelevel" %% "cats-kernel" % catsVersion,
  "org.typelevel" %% "cats-free" % catsVersion,
  "org.typelevel" %% "cats-effect" % "0.8"
)

lazy val commonSettings = Seq(
  version := "0.0.1",
  resolvers ++= Seq(
      Resolver.mavenLocal
    , Resolver.sonatypeRepo("releases")
    , Resolver.sonatypeRepo("snapshots")
    , "Bintray " at "https://dl.bintray.com/projectseptemberinc/maven"
  ),
  scalaVersion := "2.12.4",

  addCompilerPlugin("org.spire-math" %% "kind-projector"  % "0.9.4"),

  libraryDependencies ++= Seq(
      "joda-time"                     % "joda-time"                     % "2.9.1",
      "org.joda"                      % "joda-convert"                  % "1.8.1",
      "io.spray"                     %% "spray-json"                    % "1.3.2",
      "com.typesafe.akka"            %% "akka-actor"                    % "2.5.4",
      "com.typesafe.akka"            %% "akka-persistence"              % "2.5.4",
      "com.typesafe.akka"            %% "akka-stream"                   % "2.5.4",
      "com.typesafe.scala-logging"   %% "scala-logging"                 % "3.7.2",
      "com.typesafe.slick"           %% "slick"                         % "3.2.1",
      "com.h2database"                % "h2"                            % "1.4.187",
      "com.zaxxer"                    % "HikariCP-java6"                % "2.3.8",
      "ch.qos.logback"                % "logback-classic"               % "1.1.3",
      "io.monix"                     %% "monix"                         % "3.0.0-M3",
      "org.scalacheck"               %% "scalacheck"                    % "1.13.4"       % "test"
    ) ++ catsAll
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "frdomain",
    scalacOptions ++= Seq(
      "-feature",
      "-unchecked",
      "-language:higherKinds",
      "-language:postfixOps",
      "-Ypartial-unification",
      "-deprecation"
    )
  )

