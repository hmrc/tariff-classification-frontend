import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "tariff-classification-frontend"

lazy val plugins: Seq[Plugins] =
  Seq(PlayScala, SbtDistributablesPlugin)

lazy val microservice = (project in file("."))
  .enablePlugins(plugins: _*)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(majorVersion := 0)
  .settings(PlayKeys.playDefaultPort := 9581)
  .settings(
    name := appName,
    scalaVersion := "2.12.12",
    targetJvm := "jvm-1.8",
    libraryDependencies ++= AppDependencies(),
    Test / parallelExecution := false,
    Test / fork := true,
    retrieveManaged := true,
    // Use the silencer plugin to suppress warnings from unused imports in compiled twirl templates
    scalacOptions += "-P:silencer:pathFilters=views;routes",
    scalacOptions ~= { opts =>
      opts.filterNot(
        Set(
          "-Xfatal-warnings",
          "-Ywarn-value-discard"
        )
      )
    },
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % "1.7.1" % Provided cross CrossVersion.full
    )
  )
  .settings(inConfig(TemplateTest)(Defaults.testSettings): _*)
  .settings(
    Test / unmanagedSourceDirectories := Seq(
      (Test / baseDirectory).value / "test/unit",
      (Test / baseDirectory).value / "test/util"
    ),
    Test / resourceDirectory:= baseDirectory.value / "test" / "resources",
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
    RoutesKeys.routesImport ++= Seq(
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
  .settings(inConfig(TemplateItTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := true,
//    works only when fork is true
    Test / javaOptions += "-Xmx1G",
    IntegrationTest / unmanagedSourceDirectories:= Seq(
      (IntegrationTest / baseDirectory).value / "test/it",
      (IntegrationTest / baseDirectory).value / "test/util"
    ),
    IntegrationTest / resourceDirectory := baseDirectory.value / "test" / "resources",
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false
  )

  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(scalaModuleInfo := scalaModuleInfo.value map {
    _.withOverrideScalaVersion(true)
  })

lazy val allPhases   = "tt->test;test->test;test->compile;compile->compile"
lazy val allItPhases = "tit->it;it->it;it->compile;compile->compile"

lazy val TemplateTest   = config("tt") extend Test
lazy val TemplateItTest = config("tit") extend IntegrationTest

// Coverage configuration
coverageMinimumStmtTotal := 91
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;com.kenshoo.play.metrics.*;prod.*;testOnlyDoNotUseInAppConf.*;app.*;uk.gov.hmrc.BuildInfo;.*repositories.*"
