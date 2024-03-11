import uk.gov.hmrc.DefaultBuildSettings.itSettings

val appName = "tariff-classification-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.13"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    PlayKeys.playDefaultPort := 9581,
    libraryDependencies ++= AppDependencies(),
    CodeCoverageSettings.settings
  )
  .settings(
    Test / fork := true,
    Test / unmanagedSourceDirectories += baseDirectory.value / "test/util",
    Test / resourceDirectory := baseDirectory.value / "test" / "resources",
    //    works only when fork is true
    Test / javaOptions += "-Xmx1G"
  )
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat"
    )
  )
  .settings(
    play.sbt.routes.RoutesKeys.routesImport ++= Seq(
      "models.Sort",
      "models.Search",
      "models.ApplicationType",
      "models.viewmodels.AssignedToMeTab",
      "models.viewmodels.ATaRTab",
      "models.viewmodels.SubNavigationTab",
      "models.viewmodels.ManagerToolsReportsTab",
      "models.viewmodels.ManagerToolsUsersTab",
      "models.viewmodels.ManagerToolsKeywordsTab"
    )
  )
  .settings(scalacOptionsSettings)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
  .settings(scalacOptionsSettings)

lazy val scalacOptionsSettings: Seq[Setting[?]] = Seq(
  scalacOptions ++= Seq(
    "-Wconf:src=routes/.*:s",
    "-Wconf:cat=unused-imports&src=views/.*:s"
  ),
  scalacOptions ~= { opts =>
    opts.filterNot(
      Set(
        "-Xfatal-warnings",
        "-Ywarn-value-discard"
      )
    )
  }
)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle it/Test/scalastyle")
