import sbt._
object AppDependencies {
  import play.core.PlayVersion.current

  val compile = Seq(
    "uk.gov.hmrc"                %% "play-frontend-hmrc"         % "1.26.0-play-28",
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-28" % "5.16.0",
    "uk.gov.hmrc"                %% "http-caching-client"        % "9.5.0-play-28",
    "uk.gov.hmrc"                %% "simple-reactivemongo"       % "8.0.0-play-28",
    "com.typesafe.play"          %% "play-json"                  % "2.9.2",
    "uk.gov.hmrc"                %% "play-json-union-formatter"  % "1.15.0-play-28",
    "org.typelevel"              %% "cats-core"                  % "2.6.1",
    "com.github.blemale"         %% "scaffeine"                  % "4.0.2",
    "com.lightbend.akka"         %% "akka-stream-alpakka-csv"    % "1.1.2"
  )

  private lazy val scope: String = "test, it"
  val jettyVersion = "9.4.44.v20210927"

  val test = Seq(
    "com.github.tomakehurst" % "wiremock"                  % "2.27.2"         % scope,
    "com.typesafe.play"      %% "play-test"                % current          % scope,
    "org.mockito"            % "mockito-core"              % "3.11.2"         % scope,
    "org.jsoup"              % "jsoup"                     % "1.14.1"         % scope,
    "org.pegdown"            % "pegdown"                   % "1.6.0"          % scope,
    "org.scalatest"          %% "scalatest"                % "3.2.9"          % scope,
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.9.0"        % scope,
    "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.9.0"        % scope,
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % "5.9.0"          % scope,
    "uk.gov.hmrc"            %% "service-integration-test" % "1.2.0-play-28"  % scope,
    "com.vladsch.flexmark"    %  "flexmark-all"            % "0.36.8"        % scope,

    //Need to peg this version for wiremock - try removing this on next lib upgrade
    "org.eclipse.jetty" % "jetty-server"  % jettyVersion % scope,
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
