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

import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{never, reset, verify}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentMatchers, BDDMockito}
import org.mockito.BDDMockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.DecisionForm
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future

class LiabilityControllerSpec extends UnitSpec with Matchers with BeforeAndAfterEach with WithFakeApplication with MockitoSugar with ControllerCommons {

  private val decisionForm = mock[DecisionForm]
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def request[A](operator: Operator, request: Request[A]) = new AuthenticatedRequest(operator, request)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(permissions: Set[Permission]) = new LiabilityController(
    new RequestActionsWithPermissions(permissions = permissions, addViewCasePermission = false,
      c = Cases.liabilityCaseExample), decisionForm, casesService, messageApi, appConfig
  )

  "GET liability view" should {
    "redirect to unauthorised if not permitted" in {
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set.empty).liabilityDetails("ref")(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" in {
      given(decisionForm.liabilityCompleteForm(any[Decision])).willReturn(null)

      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set(Permission.VIEW_CASES)).liabilityDetails("ref")(request))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) should include("liability-heading")
    }
  }

  "GET liability edit" should {
    "redirect to unauthorised if not permitted" in {
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set.empty).editLiabilityDetails("ref")(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" in {
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set(Permission.VIEW_CASES)).editLiabilityDetails("ref")(request))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) should include("liability-details-edit-form")
    }
  }

  "POST liability edit" should {
    val updatedCase = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)

    val validReq: AuthenticatedRequest[AnyContent] = request(
      operator,
      newFakeGETRequestWithCSRF(fakeApplication).withFormUrlEncodedBody(
        "entryDate" -> "",
        "entryNumber" -> "",
        "traderName" -> "mandatory-name",
        "goodName" -> "",
        "traderCommodityCode" -> "",
        "officerCommodityCode" -> "",
        "contactName" -> "",
        "contactEmail" -> "valid@email.com",
        "contactPhone" -> ""
      )
    )

    val invalidReq: AuthenticatedRequest[AnyContent] = request(
      operator,
      newFakeGETRequestWithCSRF(fakeApplication).withFormUrlEncodedBody(
        "entryDate" -> "",
        "entryNumber" -> "",
        "traderName" -> "",
        "goodName" -> "",
        "traderCommodityCode" -> "",
        "officerCommodityCode" -> "",
        "contactName" -> "",
        "contactEmail" -> "wrongemail",
        "contactPhone" -> ""
      )
    )

    "update and redirect to liability view for permitted user" in {
      given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))
      val result = await(controller(Set(Permission.VIEW_CASES)).postLiabilityDetails("reference")(validReq))
      verify(casesService).updateCase(any[Case])(any[HeaderCarrier])
      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.LiabilityController.liabilityDetails("reference").url)
    }

    "error summary should contain expected form errors" in {
      val result: Result = await(controller(Set(Permission.VIEW_CASES)).postLiabilityDetails("reference")(invalidReq))
      verify(casesService, never()).updateCase(any[Case])(any[HeaderCarrier])
      status(result) shouldBe Status.OK
      errorSummaryShouldContains(result, Seq("#traderName", "#contactEmail"))
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(Set.empty).postLiabilityDetails("reference")(invalidReq))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  protected def errorSummaryShouldContains(result: Result, errors: Seq[String]) = {
    val doc = Jsoup.parse(contentAsString(result))
    errors.foreach(error =>
      doc.getElementById("error-summary").html should include(error)
    )
  }
}
