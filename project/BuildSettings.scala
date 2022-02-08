/*
 * Copyright (c) 2012-2020 Snowplow Analytics Ltd. All rights reserved.
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
import sbt._
import Keys._

// Scoverage
import scoverage.ScoverageKeys._

// Bintray plugin
import bintray.BintrayPlugin._
import bintray.BintrayKeys._

// Scaladocs
import sbtunidoc.ScalaUnidocPlugin.autoImport._
import com.typesafe.sbt.site.SitePlugin.autoImport._
import com.typesafe.sbt.SbtGit.GitKeys._

// Scoverage
import scoverage.ScoverageKeys._

object BuildSettings {

  lazy val publishSettings = bintraySettings ++ Seq(
    publishMavenStyle := true,
    publishArtifact := true,
    publishArtifact in Test := false,
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    bintrayOrganization := Some("snowplow"),
    bintrayRepository := "snowplow-maven",
    pomIncludeRepository := { _ => false },
    homepage := Some(url("https://github.com/snowplow-incubator/scala-lru-map")),
    scmInfo := Some(ScmInfo(url("https://github.com/snowplow-incubator/scala-lru-map"),
      "scm:git@github.com:snowplow-incubator/scala-lru-map.git")),
    pomExtra := (
      <developers>
        <developer>
          <name>Snowplow Analytics Ltd</name>
          <email>support@snowplowanalytics.com</email>
          <organization>Snowplow Analytics Ltd</organization>
          <organizationUrl>http://snowplowanalytics.com</organizationUrl>
        </developer>
      </developers>)
  )

  lazy val docSettings = Seq(
    gitRemoteRepo := "https://github.com/snowplow-incubator/scala-lru-map.git",
    siteSubdirName := ""
  )

  lazy val javaCompilerOptions = Seq("-source", "11", "-target", "11")

  lazy val coverageSettings = Seq(
    coverageMinimum := 90
  )
}
