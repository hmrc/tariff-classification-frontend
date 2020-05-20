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

import config.AppConfig
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration, Environment}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.Files.TemporaryFileCreator
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

trait SpecBase extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {

  override def fakeApplication: Application = GuiceApplicationBuilder()
    .configure(
    "metrics.jvm" -> false,
    "metrics.enabled" -> false
  ).build()

  protected lazy val injector: Injector = fakeApplication.injector

  implicit val cc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  val appConfig: AppConfig = injector.instanceOf[AppConfig]
  val config: Configuration = injector.instanceOf[Configuration]
  val env: Environment = injector.instanceOf[Environment]

  def fakeRequest = FakeRequest()

  def messages: Messages = messagesApi.preferred(fakeRequest)

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def tempFileCreator: TemporaryFileCreator = injector.instanceOf[TemporaryFileCreator]

}
