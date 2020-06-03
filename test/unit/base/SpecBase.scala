/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package base

import akka.stream.Materializer
import config.AppConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.Files.TemporaryFileCreator
import play.api.libs.ws.WSClient
import play.api.mvc.{AnyContentAsEmpty, BodyParsers, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.{Application, Configuration, Environment}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.test.UnitSpec

trait SpecBase extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach with BeforeAndAfterAll {

  override lazy val fakeApplication: Application = GuiceApplicationBuilder()
//    .disable[DataCacheConnector]
//    .disable[ReactiveRepository[_, _]]
//    .overrides(
//      bind[DataRetrievalAction].toInstance(mock[DataRetrievalAction]),
//      bind[IdentifierAction].toInstance(mock[IdentifierAction]),
//      bind[DataCacheConnector].toInstance(mock[DataCacheConnector])
//    )
    .configure(
      //trick ws client not to use a lot of connections
//      "play.ws.ahc.maxConnectionsTotal" -> 10,
//      "play.ws.ahc.maxConnectionsPerHost" -> 10,
      //turn off useless akka logging
//      "mongo-async-driver.akka.loglevel" -> "WARNING",
//      "mongo-async-driver.akka.log-dead-letters-during-shutdown" -> "off",
//      "mongo-async-driver.akka.log-dead-letters" -> "0",
      //turn off metrics
      "metrics.jvm" -> false,
      "metrics.enabled" -> false,
      //app related feature flag
      "toggle.new-liability-details" -> true
    ).build()

    lazy val appWithLiabilityToggleOff: Application = new GuiceApplicationBuilder()
      .configure(
        "metrics.jvm" -> false,
        "metrics.enabled" -> false,
        "toggle.new-liability-details" -> false
      ).build()

  lazy val mcc: MessagesControllerComponents = cc
  lazy val realAppConfig: AppConfig = injector.instanceOf[AppConfig]
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val cc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val realConfig: Configuration = injector.instanceOf[Configuration]
  lazy val realEnv: Environment = injector.instanceOf[Environment]
  lazy val realHttpAudit: HttpAuditing = injector.instanceOf[HttpAuditing]
  lazy val appConfWithLiabilityToggleOff: AppConfig =
//    mock[AppConfig]
  appWithLiabilityToggleOff.injector.instanceOf[AppConfig]
  lazy val defaultPlayBodyParsers: BodyParsers.Default = injector.instanceOf[BodyParsers.Default]
  val injector: Injector = fakeApplication.injector

  val ws: WSClient = injector.instanceOf[WSClient]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  def fakeRequestWithSessionId(sessionId: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  def tempFileCreator: TemporaryFileCreator = injector.instanceOf[TemporaryFileCreator]
  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  override protected def beforeEach(): Unit = {
    super.beforeEach()

//    reset(
//      appConfWithLiabilityToggleOff
//    )
//
//    when(appConfWithLiabilityToggleOff.newLiabilityDetails).thenReturn(false)
//    when(appConfWithLiabilityToggleOff.analyticsToken).thenReturn("g-token")
//    when(appConfWithLiabilityToggleOff.analyticsHost).thenReturn("g-host")
//    when(appConfWithLiabilityToggleOff.reportAProblemPartialUrl).thenReturn("part-url")
//    when(appConfWithLiabilityToggleOff.reportAProblemNonJSUrl).thenReturn("part-js-url")
  }

  implicit lazy val mat: Materializer = injector.instanceOf[Materializer]

  override protected def afterAll(): Unit = {
    super.afterAll()
    try {
      ws.close()
    } finally {
      Thread.sleep(1000)
    }
  }

}
