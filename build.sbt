/*
 * Copyright (c) 2012-2018 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
lazy val root = project
  .in(file("."))
  .settings(
    organization := "com.snowplowanalytics",
    name := "lru-map",
    version := "0.1.0",
    description := "Simple LRU Map for caching",
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq("2.11.11", "2.12.6"),
    scalacOptions := BuildSettings.compilerOptions,
    javacOptions := BuildSettings.javaCompilerOptions,
    libraryDependencies ++= Seq(
        Dependencies.cats,
        Dependencies.catsEffect,
        Dependencies.scalaCheck,
        Dependencies.specs2
    )
  )
  .settings(BuildSettings.publishSettings)
  .settings(BuildSettings.docSettings)
  .settings(BuildSettings.coverageSettings)
