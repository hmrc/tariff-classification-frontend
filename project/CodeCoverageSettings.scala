import sbt.Setting
import scoverage.ScoverageKeys.*

object CodeCoverageSettings {

  private val settings: Seq[Setting[?]] = Seq(
    coverageExcludedFiles := ".*Routes.*",
    coverageMinimumStmtTotal := 91.5,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )

  def apply(): Seq[Setting[?]] = settings

}
