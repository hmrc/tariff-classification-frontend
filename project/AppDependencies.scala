import sbt.*
object AppDependencies {

  private val bootstrapPlayVersion = "8.6.0"
  private val hmrcMongoPlayVersion = "2.0.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "10.1.0",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-json-union-formatter"  % "1.21.0",
    "org.typelevel"                %% "cats-core"                  % "2.12.0",
    "com.github.blemale"           %% "scaffeine"                  % "5.2.1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.17.1",
    "org.apache.pekko"             %% "pekko-connectors-csv"       % "1.0.2"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "scalacheck-1-17"        % "3.2.18.0",
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapPlayVersion
  ).map(_ % Test)

  // only add additional dependencies here - it test inherit test dependencies above already
  val itDependencies: Seq[ModuleID] = Seq()

  def apply(): Seq[ModuleID] = compile ++ test

}
