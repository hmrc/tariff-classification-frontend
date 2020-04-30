import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"          % "1.7.0",
    "uk.gov.hmrc"             %% "play-ui"                    % "8.9.0-play-26",
    "uk.gov.hmrc"             %% "play-json-union-formatter"  % "1.10.0-play-26"
  )

  val scope = "test, it"

  val jettyVersion = "9.4.27.v20200227"

  val test = Seq(
    "com.github.tomakehurst"  %  "wiremock"                 % "2.26.3"        % scope,
    "com.typesafe.play"       %% "play-test"                % current         % scope,
    "org.mockito"             %  "mockito-core"             % "2.26.0"        % scope,
    "org.jsoup"               %  "jsoup"                    % "1.11.3"        % scope,
    "org.pegdown"             %  "pegdown"                  % "1.6.0"         % scope,
    "org.scalatest"           %% "scalatest"                % "3.0.4"         % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.3"         % scope,
    "uk.gov.hmrc"             %% "hmrctest"                 % "3.9.0-play-26" % scope,
    "uk.gov.hmrc"             %% "http-verbs-test"          % "1.8.0-play-26" % scope,
    "uk.gov.hmrc"             %% "service-integration-test" % "0.9.0-play-26" % scope,
    
    //Need to peg this version for wiremock - try removing this on next lib upgrade
    "org.eclipse.jetty"       % "jetty-server"              % jettyVersion    % scope,
    "org.eclipse.jetty"       % "jetty-servlet"             % jettyVersion    % scope
  )
}
