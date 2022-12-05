import sbt._
object AppDependencies {
  import play.core.PlayVersion.current

  private val silencerVersion      = "1.7.12"
  private val bootstrapPlayVersion = "7.11.0"
  private val hmrcMongoPlayVersion = "0.73.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "3.4.0-play-28",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "http-caching-client"        % "9.6.0-play-28",
    "com.typesafe.play"            %% "play-json"                  % "2.9.3",
    "uk.gov.hmrc"                  %% "play-json-union-formatter"  % "1.15.0-play-28",
    "org.typelevel"                %% "cats-core"                  % "2.8.0",
    "com.github.blemale"           %% "scaffeine"                  % "4.1.0",
    "com.lightbend.akka"           %% "akka-stream-alpakka-csv"    % "3.0.4",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.14.0",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  private lazy val scope: String = "test, it"

  val test: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock-jre8"           % "2.33.2"             % scope,
    "com.typesafe.play"      %% "play-test"              % current              % scope,
    "org.jsoup"              % "jsoup"                   % "1.15.3"             % scope,
    "org.scalatest"          %% "scalatest"              % "3.2.14"             % scope,
    "org.scalatestplus"      %% "mockito-4-6"            % "3.2.14.0"           % scope,
    "org.scalatestplus"      %% "scalacheck-1-16"        % "3.2.14.0"           % scope,
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapPlayVersion % scope,
    "com.vladsch.flexmark"   % "flexmark-all"            % "0.62.2"             % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
