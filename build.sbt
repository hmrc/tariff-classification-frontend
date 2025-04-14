import sbt.Compile
import sbt.Keys.baseDirectory
import uk.gov.hmrc.DefaultBuildSettings.itSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.5.2"

lazy val microservice = Project("tariff-classification-frontend", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    PlayKeys.playDefaultPort := 9581,
    libraryDependencies ++= AppDependencies(),
    CodeCoverageSettings()
  )
  .settings(
    Test / fork := false,
    Test / unmanagedSourceDirectories += baseDirectory.value / "test/util",
    Test / resourceDirectory := baseDirectory.value / "test" / "resources"
  )
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat"
    )
  )
  .settings(
    routesImport ++= Seq(
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
  .settings(
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=views/.*:s"
    ),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "app" / "views" / "components" / "fop",
    Test / unmanagedResourceDirectories += baseDirectory.value / "app" / "views" / "components" / "fop"
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
