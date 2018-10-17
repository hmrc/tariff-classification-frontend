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

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.controllers.CaseController
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import unit.uk.gov.hmrc.tariffclassificationfrontend.utils.CaseExamples

import scala.concurrent.Future

class CaseControllerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val service = mock[CasesService]
  private implicit val hc = HeaderCarrier()

  private val controller = new CaseController(service, messageApi, appConfig)

  "Case Summary" should {

    "return 200 OK and HMTL content type" in {
      given(service.getOne(anyString())(any[HeaderCarrier])).willReturn(Future.successful(Some(CaseExamples.caseExample)))
      val result = controller.summary("reference")(fakeRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 404 Not Found and HMTL content type" in {
      given(service.getOne(anyString())(any[HeaderCarrier])).willReturn(Future.successful(None))
      val result = controller.summary("reference")(fakeRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

  }

}
