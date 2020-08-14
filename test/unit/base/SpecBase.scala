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
import utils.UnitSpec
import play.api.test.Helpers

trait SpecBase extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach with BeforeAndAfterAll {

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
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
  lazy val appConfWithLiabilityToggleOff: AppConfig = appWithLiabilityToggleOff.injector.instanceOf[AppConfig]
  lazy val defaultPlayBodyParsers: BodyParsers.Default = injector.instanceOf[BodyParsers.Default]
  lazy val injector: Injector = app.injector

  lazy val ws: WSClient = injector.instanceOf[WSClient]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  def fakeRequestWithSessionId(sessionId: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  def tempFileCreator: TemporaryFileCreator = injector.instanceOf[TemporaryFileCreator]
  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  implicit lazy val mat: Materializer = injector.instanceOf[Materializer]
}
