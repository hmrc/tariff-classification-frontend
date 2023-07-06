import sbt.*
object AppDependencies {

  private val bootstrapPlayVersion = "7.19.0"
  private val hmrcMongoPlayVersion = "1.3.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "7.14.0-play-28",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "http-caching-client"        % "10.0.0-play-28",
    "com.typesafe.play"            %% "play-json"                  % "2.9.4",
    "uk.gov.hmrc"                  %% "play-json-union-formatter"  % "1.18.0-play-28",
    "org.typelevel"                %% "cats-core"                  % "2.9.0",
    "com.github.blemale"           %% "scaffeine"                  % "5.2.1",
    "com.lightbend.akka"           %% "akka-stream-alpakka-csv"    % "4.0.0", // avoid upgrading for now as akka 2.7.0 breaks bobby rules due to licensing
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.15.2"
  )

  private lazy val scope: String = "test, it"

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"        %% "scalatest"              % "3.2.16",
    "org.scalatestplus"    %% "mockito-4-11"           % "3.2.16.0",
    "org.scalatestplus"    %% "scalacheck-1-17"        % "3.2.16.0",
    "uk.gov.hmrc"          %% "bootstrap-test-play-28" % bootstrapPlayVersion,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % scope)

  def apply(): Seq[ModuleID] = compile ++ test

}
