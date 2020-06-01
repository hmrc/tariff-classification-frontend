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
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration, Environment}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.Files.TemporaryFileCreator
import play.api.mvc.{AnyContentAsEmpty, BodyParsers, MessagesControllerComponents}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.test.UnitSpec

trait SpecBase extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {

  override def fakeApplication: Application = GuiceApplicationBuilder()
    .configure(
    "metrics.jvm" -> false,
      "metrics.enabled" -> false,
      "toggle.new-liability-details" -> true
  ).build()

  protected lazy val injector: Injector = fakeApplication.injector

  //real
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val cc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val mcc: MessagesControllerComponents = cc
  lazy val realAppConfig: AppConfig = injector.instanceOf[AppConfig]
  lazy val realConfig: Configuration = injector.instanceOf[Configuration]
  lazy val realEnv: Environment = injector.instanceOf[Environment]
  lazy val realHttpAudit: HttpAuditing = injector.instanceOf[HttpAuditing]

  //mocks

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  def fakeRequestWithSessionId(sessionId: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def tempFileCreator: TemporaryFileCreator = injector.instanceOf[TemporaryFileCreator]

  implicit lazy val mat: Materializer = injector.instanceOf[Materializer]
  lazy val defaultPlayBodyParsers: BodyParsers.Default = injector.instanceOf[BodyParsers.Default]
//  def inject[T](implicit m: Manifest[T]): T = injector.instanceOf[T]

}
