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

// dynver plugin
import sbtdynver.DynVerPlugin.autoImport._

// Mima plugin
import com.typesafe.tools.mima.plugin.MimaKeys._

// Scoverage
import scoverage.ScoverageKeys._

// Scaladocs
import com.typesafe.sbt.site.SitePlugin.autoImport._
import com.typesafe.sbt.SbtGit.GitKeys._

object BuildSettings {

  lazy val publishSettings = Seq[Setting[_]](
    publishArtifact        := true,
    Test / publishArtifact := false,
    pomIncludeRepository   := { _ => false },
    homepage               := Some(url("http://snowplowanalytics.com")),
    licenses               += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    ThisBuild / dynverVTagPrefix := false, // Otherwise git tags required to have v-prefix
    developers := List(
      Developer(
        "Snowplow Analytics Ltd",
        "Snowplow Analytics Ltd",
        "support@snowplowanalytics.com",
        url("https://snowplowanalytics.com")
      )
    )
  )

  lazy val docSettings = Seq(
    gitRemoteRepo  := "https://github.com/snowplow-incubator/scala-lru-map.git",
    siteSubdirName := ""
  )

  lazy val javaCompilerOptions = Seq("-source", "11", "-target", "11")

  lazy val coverageSettings = Seq(
    coverageMinimumStmtTotal := 90,
    coverageFailOnMinimum    := false,
    (Test / test) := {
      (coverageReport dependsOn (Test / test)).value
    }
  )

  // If a new version introduces breaking changes,
  // clear `mimaBinaryIssueFilters` and `mimaPreviousVersions`.
  // Otherwise, add previous version to the set without
  // removing older versions.
  lazy val mimaPreviousVersions = Set("0.5.0")
  lazy val mimaSettings = Seq(
    mimaPreviousArtifacts := mimaPreviousVersions.map { organization.value %% name.value % _ },
    ThisBuild / mimaFailOnNoPrevious := false,
    mimaBinaryIssueFilters ++= Seq(),
    Test / test := {
      mimaReportBinaryIssues.value
      (Test / test).value
    }
  )
}
