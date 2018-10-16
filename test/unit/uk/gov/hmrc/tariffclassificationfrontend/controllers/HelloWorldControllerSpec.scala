/*
 * Copyright 2018 HM Revenue & Customs
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

package unit.uk.gov.hmrc.tariffclassificationfrontend.controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.tariffclassificationfrontend.config.{APIDependencyConfig, AppConfig}
import uk.gov.hmrc.tariffclassificationfrontend.controllers.HelloWorld


class HelloWorldControllerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar {
  val fakeRequest = FakeRequest()

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  private val serviceConfig = mock[APIDependencyConfig]

  val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  val appConfig = new AppConfig(serviceConfig, configuration, env)

  val controller = new HelloWorld(messageApi, appConfig)

  "Hello World" should {

    "return 200" in {
      val result = controller.helloWorld(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = controller.helloWorld(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

  }

}