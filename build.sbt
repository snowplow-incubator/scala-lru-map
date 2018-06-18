lazy val root = project
  .in(file("."))
  .settings(
    organization := "com.snowplowanalytics",
    name := "lru-map",
    version := "0.1.0",
    description := "Simple LRU Map for caching",
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
        Dependencies.cats,
        Dependencies.catsEffect,
        Dependencies.scalaCheck,
        Dependencies.specs2
    )
  )
