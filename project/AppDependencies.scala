import play.core.PlayVersion.current
import sbt._
object AppDependencies {
  val compile = Seq(
    "uk.gov.hmrc"        %% "play-frontend-hmrc"         % "1.22.0-play-27",
    "uk.gov.hmrc"        %% "bootstrap-frontend-play-27" % "5.16.0",
    "uk.gov.hmrc"        %% "http-caching-client"        % "9.5.0-play-27",
    "uk.gov.hmrc"        %% "simple-reactivemongo"       % "8.0.0-play-27",
    "com.typesafe.play"  %% "play-json"                  % "2.9.2",
    "uk.gov.hmrc"        %% "play-json-union-formatter"  % "1.15.0-play-27",
    "org.typelevel"      %% "cats-core"                  % "2.6.1",
    "com.github.blemale" %% "scaffeine"                  % "4.0.2",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv"    % "1.1.2"
  )

  val scope = "test, it"

  val jettyVersion = "9.4.44.v20210927"

  val test = Seq(
    "com.github.tomakehurst" % "wiremock"                  % "2.27.2"         % scope,
    "com.typesafe.play"      %% "play-test"                % current          % scope,
    "org.mockito"            % "mockito-core"              % "2.26.0"         % scope,
    "org.jsoup"              % "jsoup"                     % "1.13.1"         % scope,
    "org.pegdown"            % "pegdown"                   % "1.6.0"          % scope,
    "org.scalatest"          %% "scalatest"                % "3.0.9"          % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "4.0.3"          % scope,
    "org.scalacheck"         %% "scalacheck"               % "1.15.4"         % scope,
    "uk.gov.hmrc"            %% "http-verbs-test"          % "1.8.0-play-27"  % scope,
    "uk.gov.hmrc"            %% "service-integration-test" % "1.2.0-play-27" % scope,
    //Need to peg this version for wiremock - try removing this on next lib upgrade
    "org.eclipse.jetty" % "jetty-server"  % jettyVersion % scope,
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % scope
  )
}
