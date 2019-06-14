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
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import play.api.data.validation.{Constraint, Valid}
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{CommodityCodeConstraints, DecisionForm}
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases._

import scala.concurrent.Future

class LiabilityControllerSpec extends UnitSpec with Matchers with BeforeAndAfterEach with WithFakeApplication with MockitoSugar with ControllerCommons {

  private val commodityCodeConstraints = mock[CommodityCodeConstraints]
  private val decisionForm = new DecisionForm(commodityCodeConstraints)
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

  private def controller(permissions: Set[Permission], c: Case) = new LiabilityController(
    new RequestActionsWithPermissions(permissions = permissions, addViewCasePermission = false, c = c),
    decisionForm, messageApi, casesService, appConfig
  )

  "GET liability view" should {
    val openCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())

    "redirect to unauthorised if not permitted" in {
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set.empty, openCase).liabilityDetails("ref")(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" in {
      given(commodityCodeConstraints.commodityCodeExistsInUKTradeTariff).willReturn(Constraint[String]("error")(_ => Valid))

      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set(Permission.VIEW_CASES), openCase).liabilityDetails("ref")(request))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) should include("liability-heading")
    }
  }

  "GET liability edit" should {
    "redirect to unauthorised if not permitted" in {
      val openCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set.empty, openCase).editLiabilityDetails("ref")(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" when {
      "Case has status NEW" in {
        val c = aCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())
        val request = newFakeGETRequestWithCSRF(fakeApplication)
        val result = await(controller(Set(Permission.VIEW_CASES), c).editLiabilityDetails("ref")(request))

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")

        contentAsString(result) should include("liability-details-edit-form")
      }

      "Case has status OPEN and Edit Access" in {
        val c = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
        val request = newFakeGETRequestWithCSRF(fakeApplication)
        val result = await(controller(Set(Permission.VIEW_CASES, Permission.EDIT_LIABILITY), c).editLiabilityDetails("ref")(request))

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")

        contentAsString(result) should include("liability-details-edit-form")
      }
    }
  }

  "POST liability edit" should {
    val updatedCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())

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

    "update and redirect to liability view" when {
      "case status is NEW" in {
        given(commodityCodeConstraints.commodityCodeExistsInUKTradeTariff).willReturn(Constraint[String]("error")(_ => Valid))
        given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))

        val c = aCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())
        val result = await(controller(Set(Permission.VIEW_CASES), c).postLiabilityDetails("reference")(validReq))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.LiabilityController.liabilityDetails("reference").url)
        verify(casesService).updateCase(any[Case])(any[HeaderCarrier])
      }

      "case status is OPEN with permission" in {
        given(commodityCodeConstraints.commodityCodeExistsInUKTradeTariff).willReturn(Constraint[String]("error")(_ => Valid))
        given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))

        val c = aCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())
        val result = await(controller(Set(Permission.VIEW_CASES, Permission.EDIT_LIABILITY), c).postLiabilityDetails("reference")(validReq))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.LiabilityController.liabilityDetails("reference").url)
        verify(casesService).updateCase(any[Case])(any[HeaderCarrier])
      }
    }

    "error summary should contain expected form errors" in {
      val openCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
      val result: Result = await(controller(Set(Permission.VIEW_CASES, Permission.EDIT_LIABILITY), openCase).postLiabilityDetails("reference")(invalidReq))
      verify(casesService, never()).updateCase(any[Case])(any[HeaderCarrier])
      status(result) shouldBe Status.OK
      errorSummaryShouldContains(result, Seq("#traderName"))
    }

    "redirect unauthorised when does not have right permissions" in {
      val openCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())
      val result: Result = await(controller(Set(), openCase).postLiabilityDetails("reference")(invalidReq))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "redirect to default" when {
      for(s: CaseStatus <- CaseStatus.values.filterNot(_ == CaseStatus.OPEN).filterNot(_ == CaseStatus.NEW)) {

        s"case status is $s" in {
          val c = aCase(withReference("reference"), withStatus(s), withLiabilityApplication())
          val result: Result = await(controller(Set(Permission.VIEW_CASES), c).postLiabilityDetails("reference")(invalidReq))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaseController.get("reference").url)
        }
      }
    }
  }

  protected def errorSummaryShouldContains(result: Result, errors: Seq[String]) = {
    val doc = Jsoup.parse(contentAsString(result))
    errors.foreach(error =>
      doc.getElementById("error-summary").html should include(error)
    )
  }
}
