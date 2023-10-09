import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption

val appName = "tariff-classification-frontend"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(majorVersion := 0)
  .settings(PlayKeys.playDefaultPort := 9581)
  .settings(
    name := appName,
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies(),
    Test / fork := true,
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=html/.*:s"
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
  .settings(inConfig(TemplateTest)(Defaults.testSettings) *)
  .settings(
    Test / unmanagedSourceDirectories := Seq(
      (Test / baseDirectory).value / "test/unit",
      (Test / baseDirectory).value / "test/util"
    ),
    Test / resourceDirectory := baseDirectory.value / "test" / "resources",
    //    works only when fork is true
    Test / javaOptions += "-Xmx1G",
    addTestReportOption(Test, "test-reports")
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
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings))
  .settings(inConfig(TemplateItTest)(Defaults.itSettings) *)
  .settings(
    IntegrationTest / fork := true,
    //    works only when fork is true
    Test / javaOptions += "-Xmx1G",
    IntegrationTest / unmanagedSourceDirectories := Seq(
      (IntegrationTest / baseDirectory).value / "test/it",
      (IntegrationTest / baseDirectory).value / "test/util"
    ),
    IntegrationTest / resourceDirectory := baseDirectory.value / "test" / "resources",
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
  .settings(scalaModuleInfo := scalaModuleInfo.value map {
    _.withOverrideScalaVersion(true)
  })

lazy val allPhases   = "tt->test;test->test;test->compile;compile->compile"
lazy val allItPhases = "tit->it;it->it;it->compile;compile->compile"

lazy val TemplateTest   = config("tt") extend Test
lazy val TemplateItTest = config("tit") extend IntegrationTest

// Coverage configuration
coverageMinimumStmtTotal := 92
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;com.kenshoo.play.metrics.*;prod.*;testOnlyDoNotUseInAppConf.*;app.*;uk.gov.hmrc.BuildInfo;"
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt IntegrationTest/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle IntegrationTest/scalastyle")
