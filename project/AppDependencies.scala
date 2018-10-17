import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-25"          % "3.10.0",
    "uk.gov.hmrc"             %% "govuk-template"             % "5.23.0",
    "uk.gov.hmrc"             %% "play-ui"                    % "7.22.0",
    "uk.gov.hmrc"             %% "play-json-union-formatter"  % "1.4.0"
  )

  val test = Seq(
    "com.github.tomakehurst"  %  "wiremock"                 % "2.19.0"    % "test, it",
    "com.typesafe.play"       %% "play-test"                % current     % "test, it",
    "org.assertj"             %  "assertj-core"             % "3.11.1"    % "test, it",
    "org.mockito"             %  "mockito-core"             % "2.23.0"    % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"     % "test, it",
    "org.scalatest"           %% "scalatest"                % "3.0.4"     % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "2.0.1"     % "test, it",
    "uk.gov.hmrc"             %% "hmrctest"                 % "3.2.0"     % "test, it",
    "uk.gov.hmrc"             %% "http-verbs-test"          % "1.1.0"     % "test, it",
    "uk.gov.hmrc"             %% "service-integration-test" % "0.3.0"     % "test, it"
  )

}
