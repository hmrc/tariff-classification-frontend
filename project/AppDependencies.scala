import sbt._
object AppDependencies {
  import play.core.PlayVersion.current

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"        %% "play-frontend-hmrc"         % "3.22.0-play-28",
    "uk.gov.hmrc"        %% "bootstrap-frontend-play-28" % "6.3.0",
    "uk.gov.hmrc"        %% "http-caching-client"        % "9.6.0-play-28",
    "uk.gov.hmrc"        %% "simple-reactivemongo"       % "8.1.0-play-28",
    "com.typesafe.play"  %% "play-json"                  % "2.9.2",
    "uk.gov.hmrc"        %% "play-json-union-formatter"  % "1.15.0-play-28",
    "org.typelevel"      %% "cats-core"                  % "2.8.0",
    "com.github.blemale" %% "scaffeine"                  % "4.0.2",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv"    % "3.0.4"
  )

  private lazy val scope: String = "test, it"
  val jettyVersion               = "9.4.44.v20210927"

  val test: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock"                  % "2.27.2"        % scope,
    "com.typesafe.play"      %% "play-test"                % current         % scope,
    "org.mockito"            % "mockito-core"              % "4.6.1"         % scope,
    "org.jsoup"              % "jsoup"                     % "1.15.2"        % scope,
    "org.pegdown"            % "pegdown"                   % "1.6.0"         % scope,
    "org.scalatest"          %% "scalatest"                % "3.2.12"        % scope,
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.10.0"      % scope,
    "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.11.0"      % scope,
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % "6.3.0"         % scope,
    "uk.gov.hmrc"            %% "service-integration-test" % "1.3.0-play-28" % scope,
    "com.vladsch.flexmark"   % "flexmark-all"              % "0.62.2"        % scope,
    //Need to peg this version for wiremock - try removing this on next lib upgrade
    "org.eclipse.jetty" % "jetty-server"  % jettyVersion % scope,
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
