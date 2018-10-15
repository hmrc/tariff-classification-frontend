import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "govuk-template"           % "5.23.0",
    "uk.gov.hmrc"             %% "play-ui"                  % "7.22.0",
    "uk.gov.hmrc"             %% "bootstrap-play-25"        % "3.9.0"
  )

  val test = Seq(
    "org.assertj"             % "assertj-core"              % "3.11.1"                % "test, it",
    "org.mockito"             % "mockito-core"              % "2.23.0"                % "test",
    "uk.gov.hmrc"             %% "hmrctest"                 % "3.0.0"                 % "test, it",
    "org.scalatest"           %% "scalatest"                % "3.0.4"                 % "test, it",
    "com.typesafe.play"       %% "play-test"                % current                 % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "uk.gov.hmrc"             %% "service-integration-test" % "0.2.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "2.0.0"                 % "test, it"
  )

}
