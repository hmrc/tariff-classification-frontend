import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-25"          % "4.8.0",
    "uk.gov.hmrc"             %% "play-ui"                    % "7.32.0-play-25",
    "uk.gov.hmrc"             %% "play-json-union-formatter"  % "1.5.0"
  )

  val scope = "test, it"

  val test = Seq(
    "com.github.tomakehurst"  %  "wiremock"                 % "2.21.0"        % scope,
    "com.typesafe.play"       %% "play-test"                % current         % scope,
    "org.mockito"             %  "mockito-core"             % "2.24.0"        % scope,
    "org.jsoup"               %  "jsoup"                    % "1.11.3"        % scope,
    "org.pegdown"             %  "pegdown"                  % "1.6.0"         % scope,
    "org.scalatest"           %% "scalatest"                % "3.0.4"         % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "2.0.1"         % scope,
    "uk.gov.hmrc"             %% "hmrctest"                 % "3.4.0-play-25" % scope,
    "uk.gov.hmrc"             %% "http-verbs-test"          % "1.2.0"         % scope,
    "uk.gov.hmrc"             %% "service-integration-test" % "0.5.0-play-25" % scope
  )

}
