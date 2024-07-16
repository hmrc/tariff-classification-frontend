import sbt.*
object AppDependencies {

  private val bootstrapPlayVersion = "8.6.0"
  private val hmrcMongoPlayVersion = "2.1.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "10.4.0",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-json-union-formatter"  % "1.21.0",
    "org.typelevel"                %% "cats-core"                  % "2.12.0",
    "com.github.blemale"           %% "scaffeine"                  % "5.3.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.17.2",
    "org.apache.pekko"             %% "pekko-connectors-csv"       % "1.0.2"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "scalacheck-1-18"        % "3.2.19.0",
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapPlayVersion
  ).map(_ % Test)

  // only add additional dependencies here - it test inherit test dependencies above already
  val itDependencies: Seq[ModuleID] = Seq()

  def apply(): Seq[ModuleID] = compile ++ test

}
