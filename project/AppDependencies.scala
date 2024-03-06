import sbt.*
object AppDependencies {

  private val bootstrapPlayVersion = "8.4.0"
  private val hmrcMongoPlayVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "8.5.0",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "com.typesafe.play"            %% "play-json"                  % "2.9.4",
    "uk.gov.hmrc"                  %% "play-json-union-formatter"  % "1.18.0-play-28",
    "org.typelevel"                %% "cats-core"                  % "2.10.0",
    "com.github.blemale"           %% "scaffeine"                  % "5.2.1",
    "com.lightbend.akka"           %% "akka-stream-alpakka-csv"    % "4.0.0", // avoid upgrading for now as akka 2.7.0 breaks bobby rules due to licensing
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.15.3"
  )


  val test: Seq[ModuleID] = Seq(
    "org.scalatest"        %% "scalatest"              % "3.2.17",
    "org.scalatestplus"    %% "mockito-4-11"           % "3.2.17.0",
    "org.scalatestplus"    %% "scalacheck-1-17"        % "3.2.17.0",
    "uk.gov.hmrc"          %% "bootstrap-test-play-30" % bootstrapPlayVersion,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
