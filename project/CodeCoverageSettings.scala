import sbt.Setting
import scoverage.ScoverageKeys.*

object CodeCoverageSettings {

  val settings: Seq[Setting[?]] = Seq(
    coverageMinimumStmtTotal := 100,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,
    coverageExcludedPackages := "<empty>;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*" +
      ";app.*;uk.gov.hmrc.BuildInfo;.*.Routes;.*.RoutesPrefix"
  )
}