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

import models.forms.{CommodityCodeConstraints, DecisionForm}
import models.request.AuthenticatedRequest
import models.{Permission, _}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{never, reset, verify}
import org.scalatest.BeforeAndAfterEach
import play.api.data.validation.{Constraint, Valid}
import play.api.http.Status
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers._
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class LiabilityControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val commodityCodeConstraints = mock[CommodityCodeConstraints]
  private val decisionForm             = new DecisionForm(commodityCodeConstraints)
  private val casesService             = mock[CasesService]
  private val operator                 = mock[Operator]

  private def request[A](operator: Operator, request: Request[A]) = new AuthenticatedRequest(operator, request)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(permissions: Set[Permission], c: Case) = new LiabilityController(
    new RequestActionsWithPermissions(playBodyParsers, permissions = permissions, addViewCasePermission = false, c = c),
    decisionForm,
    mcc,
    casesService,
    realAppConfig
  )

  "GET liability view" should {
    val openCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())

    "redirect to unauthorised if not permitted" in {
      val request = newFakeGETRequestWithCSRF(app)
      val result  = await(controller(Set.empty, openCase).liabilityDetails("ref")(request))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" in {
      given(commodityCodeConstraints.commodityCodeNumeric)
        .willReturn(Constraint[String]("error")(_ => Valid))

      val request = newFakeGETRequestWithCSRF(app)
      val result  = await(controller(Set(Permission.VIEW_CASES), openCase).liabilityDetails("ref")(request))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

      contentAsString(result) should include("liability-heading")
    }
  }

  "GET liability edit" should {
    "redirect to unauthorised if not permitted" in {
      val openCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
      val request  = newFakeGETRequestWithCSRF(app)
      val result   = await(controller(Set.empty, openCase).editLiabilityDetails("ref")(request))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" when {

      "Permitted with Edit Access" in {
        val c       = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
        val request = newFakeGETRequestWithCSRF(app)
        val result = await(
          controller(Set(Permission.VIEW_CASES, Permission.EDIT_LIABILITY), c).editLiabilityDetails("ref")(request)
        )

        status(result)      shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result)     shouldBe Some("utf-8")

        contentAsString(result) should include("liability-details-edit-form")
      }
    }
  }

  "POST liability edit" should {
    val updatedCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())

    val validReq: AuthenticatedRequest[AnyContent] = request(
      operator,
      newFakeGETRequestWithCSRF(app).withFormUrlEncodedBody(
        "entryDate"            -> "",
        "entryNumber"          -> "",
        "traderName"           -> "mandatory-name",
        "goodName"             -> "",
        "traderCommodityCode"  -> "",
        "officerCommodityCode" -> "",
        "contactName"          -> "",
        "contactEmail"         -> "valid@email.com",
        "contactPhone"         -> ""
      )
    )

    val invalidReq: AuthenticatedRequest[AnyContent] = request(
      operator,
      newFakeGETRequestWithCSRF(app).withFormUrlEncodedBody(
        "entryDate"            -> "",
        "entryNumber"          -> "",
        "traderName"           -> "",
        "goodName"             -> "",
        "traderCommodityCode"  -> "",
        "officerCommodityCode" -> "",
        "contactName"          -> "",
        "contactEmail"         -> "wrongemail",
        "contactPhone"         -> ""
      )
    )

    "update and redirect to liability view" when {
      "request has permission" in {
        given(commodityCodeConstraints.commodityCodeNumeric)
          .willReturn(Constraint[String]("error")(_ => Valid))
        given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))

        val c = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
        val result = await(
          controller(Set(Permission.VIEW_CASES, Permission.EDIT_LIABILITY), c)
            .postLiabilityDetails("reference")(validReq)
        )

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.LiabilityController.liabilityDetails("reference").url)
        verify(casesService).updateCase(any[Case])(any[HeaderCarrier])
      }
    }

    "error summary should contain expected form errors" in {
      val openCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
      val result: Result = await(
        controller(Set(Permission.VIEW_CASES, Permission.EDIT_LIABILITY), openCase)
          .postLiabilityDetails("reference")(invalidReq)
      )
      verify(casesService, never()).updateCase(any[Case])(any[HeaderCarrier])
      status(result) shouldBe Status.OK
      errorSummaryShouldContains(result, Seq("#traderName"))
    }

    "redirect unauthorised when does not have right permissions" in {
      val openCase       = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
      val result: Result = await(controller(Set(), openCase).postLiabilityDetails("reference")(invalidReq))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }
  }

  protected def errorSummaryShouldContains(result: Result, errors: Seq[String]) = {
    val doc = Jsoup.parse(contentAsString(result))
    errors.foreach(error => doc.getElementById("error-summary").html should include(error))
  }
}
