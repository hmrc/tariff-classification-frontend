import sbt.*
object AppDependencies {

  private val bootstrapPlayVersion = "8.4.0"
  private val hmrcMongoPlayVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "8.5.0",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "org.playframework"            %% "play-json"                  % "3.0.2",
    "uk.gov.hmrc"                  %% "play-json-union-formatter"  % "1.21.0",
    "org.typelevel"                %% "cats-core"                  % "2.10.0",
    "com.github.blemale"           %% "scaffeine"                  % "5.2.1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.15.3",
    "org.apache.pekko"             %% "pekko-connectors-csv"       % "1.0.2"
  )

  private lazy val scope: String = "test, it"

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"        %% "scalatest"              % "3.2.17",
    "org.scalatestplus"    %% "mockito-4-11"           % "3.2.17.0",
    "org.scalatestplus"    %% "scalacheck-1-17"        % "3.2.17.0",
    "uk.gov.hmrc"          %% "bootstrap-test-play-30" % bootstrapPlayVersion,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % scope)

  def apply(): Seq[ModuleID] = compile ++ test

}
