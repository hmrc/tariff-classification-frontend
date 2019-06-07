/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.LiabilityForm
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views.html.create_liability
import uk.gov.tariffclassificationfrontend.utils.Cases._

import scala.concurrent.Future._

class CreateLiabilityControllerSpec extends UnitSpec with Matchers with WithFakeApplication with MockitoSugar with ControllerCommons {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(permission: Set[Permission]) = new CreateLiabilityController(
    new RequestActionsWithPermissions(permission), casesService, messageApi, appConfig
  )

  "GET" should {
    "redirect to unauthorised if not permitted" in {
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set.empty).get()(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" in {
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set(Permission.CREATE_CASES)).get()(request))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) shouldBe create_liability(LiabilityForm.newLiabilityForm)(request, messageApi.preferred(request), appConfig).toString()
    }
  }

  "POST" should {
    "redirect to unauthorised if not permitted" in {
      val request = newFakePOSTRequestWithCSRF(fakeApplication)
      val result = await(controller(Set.empty).post()(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "render view with errors" when {
      "form is invalid" in {
        val request = newFakePOSTRequestWithCSRF(fakeApplication)
        val result = await(controller(Set(Permission.CREATE_CASES)).post()(request))
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
        contentAsString(result) shouldBe create_liability(
          LiabilityForm.newLiabilityForm.bindFromRequest()(request)
        )(request, messageApi.preferred(request), appConfig).toString()
      }
    }

    "redirect on success" in {
      given(casesService.createCase(any[LiabilityOrder])(any[HeaderCarrier])).willReturn(successful(aCase(withReference("reference"))))
      val request = newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody(
        "trader-name" -> "Trader",
        "liability-status" -> "LIVE"
      )

      val result = await(controller(Set(Permission.CREATE_CASES)).post()(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CaseController.get("reference").url)
    }
  }

}
