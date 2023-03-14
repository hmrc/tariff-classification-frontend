import sbt._
object AppDependencies {

  private val bootstrapPlayVersion = "7.14.0"
  private val hmrcMongoPlayVersion = "0.74.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "6.8.0-play-28",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "http-caching-client"        % "10.0.0-play-28",
    "com.typesafe.play"            %% "play-json"                  % "2.9.4",
    "uk.gov.hmrc"                  %% "play-json-union-formatter"  % "1.18.0-play-28",
    "org.typelevel"                %% "cats-core"                  % "2.9.0",
    "com.github.blemale"           %% "scaffeine"                  % "5.2.1",
    "com.lightbend.akka"           %% "akka-stream-alpakka-csv"    % "4.0.0", // avoid upgrading for now as akka 2.7.0 breaks bobby rules due to licensing
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.14.2"
  )

  private lazy val scope: String = "test, it"

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"    %% "play-test"              % "2.8.19"             % scope,
    "org.scalatest"        %% "scalatest"              % "3.2.15"             % scope,
    "org.scalatestplus"    %% "mockito-4-6"            % "3.2.15.0"           % scope,
    "org.scalatestplus"    %% "scalacheck-1-17"        % "3.2.15.0"           % scope,
    "uk.gov.hmrc"          %% "bootstrap-test-play-28" % bootstrapPlayVersion % scope,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.0"             % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
