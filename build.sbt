import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "tariff-classification-frontend"

lazy val plugins: Seq[Plugins] =
  Seq(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val microservice = (project in file("."))
  .enablePlugins(plugins: _*)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(majorVersion := 0)
  .settings(PlayKeys.playDefaultPort := 9581)
  .settings(
    name := appName,
    scalaVersion := "2.12.12",
    targetJvm := "jvm-1.8",
    libraryDependencies ++= (AppDependencies.compile ++ AppDependencies.test).map(_ withSources ()),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    parallelExecution in Test := false,
    fork in Test := true,
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
    unmanagedSourceDirectories in Test := Seq(
      (baseDirectory in Test).value / "test/unit",
      (baseDirectory in Test).value / "test/util"
    ),
    resourceDirectory in Test := baseDirectory.value / "test" / "resources",
//    works only when fork is true
    javaOptions in Test += "-Xmx1G",
    addTestReportOption(Test, "test-reports")
  )
  .settings(RoutesKeys.routesImport += "models.Sort")
  .settings(RoutesKeys.routesImport += "models.ApplicationType")
  .settings(RoutesKeys.routesImport += "models.ApplicationType._")
  .settings(RoutesKeys.routesImport += "controllers.ActiveTab")
  .settings(RoutesKeys.routesImport += "models.Search")
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings))
  .settings(inConfig(TemplateItTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := true,
//    works only when fork is true
    javaOptions in Test += "-Xmx1G",
    unmanagedSourceDirectories in IntegrationTest := Seq(
      (baseDirectory in IntegrationTest).value / "test/it",
      (baseDirectory in IntegrationTest).value / "test/util"
    ),
    resourceDirectory in IntegrationTest := baseDirectory.value / "test" / "resources",
    addTestReportOption(IntegrationTest, "int-test-reports"),
    parallelExecution in IntegrationTest := false
  )
  .settings(
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    resolvers += Resolver.jcenterRepo
  )
  .settings(scalaModuleInfo := scalaModuleInfo.value map {
    _.withOverrideScalaVersion(true)
  })

lazy val allPhases   = "tt->test;test->test;test->compile;compile->compile"
lazy val allItPhases = "tit->it;it->it;it->compile;compile->compile"

lazy val TemplateTest   = config("tt") extend Test
lazy val TemplateItTest = config("tit") extend IntegrationTest

//def unitFilter(name: String): Boolean = name startsWith "unit"
//
//def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = {
//  tests map { test =>
//    val forkOpts = ForkOptions().withRunJVMOptions(Vector("-Dtest.name=" + test.name))
//    Group(test.name, Seq(test), SubProcess(forkOpts))
//  }
//}

// Coverage configuration
coverageMinimum := 94
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;com.kenshoo.play.metrics.*;prod.*;testOnlyDoNotUseInAppConf.*;app.*;uk.gov.hmrc.BuildInfo;.*repositories.*"
