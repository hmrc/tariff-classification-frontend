import sbt.*

object AppDependencies {
  
  private val hmrcMongoPlayVersion = "2.6.0"
  private val bootstrapPlayVersion = "9.11.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "12.0.0",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "org.typelevel"                %% "cats-core"                  % "2.13.0",
    "com.github.blemale"           %% "scaffeine"                  % "5.3.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.18.3",
    "org.apache.pekko"             %% "pekko-connectors-csv"       % "1.0.2",
    "commons-io"                    % "commons-io"                 % "2.18.0",
    "org.apache.xmlgraphics"        % "fop"                        % "2.10",
    "net.sf.saxon"                  % "Saxon-HE"                   % "12.5"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "scalacheck-1-18"        % "3.2.19.0",
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapPlayVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
