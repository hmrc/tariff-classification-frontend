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

package controllers

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatest.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.{BodyParsers, MessagesControllerComponents}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import config.AppConfig
import models.forms.LiabilityForm
import models.{Permission, _}
import service.CasesService
import views.html.create_liability
import utils.Cases._

import scala.concurrent.Future._

class CreateLiabilityControllerSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {

  private val messageApi = inject[MessagesControllerComponents]
  private val appConfig = inject[AppConfig]
  private val casesService = mock[CasesService]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(permission: Set[Permission]) = new CreateLiabilityController(
    new RequestActionsWithPermissions(inject[BodyParsers.Default], permission), casesService, messageApi, appConfig
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
      contentAsString(result) shouldBe create_liability(
        LiabilityForm.newLiabilityForm)(request, messageApi.messagesApi.preferred(request), appConfig).toString()
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
        )(request, messageApi.messagesApi.preferred(request), appConfig).toString()
      }
    }

    "redirect on success" in {
      given(casesService.createCase(any[LiabilityOrder])(any[HeaderCarrier]))
        .willReturn(successful(aCase(withReference("reference"))))
      val request = newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody(
        "item-name" -> "item name",
        "trader-name" -> "Trader",
        "liability-status" -> "LIVE"
      )

      val result = await(controller(Set(Permission.CREATE_CASES)).post()(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CaseController.get("reference").url)
    }
  }

}
