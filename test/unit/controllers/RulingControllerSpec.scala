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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.validation.{Constraint, Valid}
import play.api.http.Status
import play.api.mvc.{BodyParsers, MessagesControllerComponents}
import play.api.test.Helpers.{redirectLocation, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import config.AppConfig
import models.forms.{CommodityCodeConstraints, DecisionForm, DecisionFormMapper}
import models.{Case, CaseStatus, Operator, Permission}
import service.{CasesService, CommodityCodeService, FileStoreService}
import utils.Cases._

import scala.concurrent.Future

class RulingControllerSpec extends UnitSpec
  with Matchers
  with GuiceOneAppPerSuite
  with MockitoSugar
  with BeforeAndAfterEach
  with ControllerCommons {

  private val messagesControllerComponents = inject[MessagesControllerComponents]
  private val appConfig = inject[AppConfig]
  private val casesService = mock[CasesService]
  private val fileService = mock[FileStoreService]
  private val mapper = mock[DecisionFormMapper]
  private val commodityCodeService = mock[CommodityCodeService]
  private val operator = mock[Operator]
  private val commodityCodeConstraints = mock[CommodityCodeConstraints]
  private val decisionForm = new DecisionForm(commodityCodeConstraints)

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  private def controller(c: Case) = new RulingController(
    new SuccessfulRequestActions(inject[BodyParsers.Default], operator, c = c), casesService, fileService, mapper, decisionForm, messagesControllerComponents, appConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new RulingController(
    new RequestActionsWithPermissions(inject[BodyParsers.Default], permission, c = requestCase), casesService, fileService, mapper, decisionForm, messagesControllerComponents, appConfig)


  "Edit Ruling" should {
    val btiCaseWithStatusNEW = aCase(withBTIApplication, withReference("reference"), withStatus(CaseStatus.NEW))
    val btiCaseWithStatusOPEN = aCase(withBTIApplication, withReference("reference"), withStatus(CaseStatus.OPEN))
    val liabilityCaseWithStatusOPEN = aCase(withLiabilityApplication(), withReference("reference"), withStatus(CaseStatus.OPEN))
    val attachment = storedAttachment

    "return OK and HTML content type" when {
      "Case is a BTI" in {
        given(fileService.getAttachments(refEq(btiCaseWithStatusOPEN))(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

        val result = controller(btiCaseWithStatusOPEN).editRulingDetails("reference")(newFakeGETRequestWithCSRF(fakeApplication))
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
        contentAsString(result) should (include("Ruling") and include("<form"))
      }

      "Case is a Liability" in {
        given(commodityCodeConstraints.commodityCodeExistsInUKTradeTariff).willReturn(Constraint[String]("error")( _ =>  Valid))
        val result = controller(liabilityCaseWithStatusOPEN).editRulingDetails("reference")(newFakeGETRequestWithCSRF(fakeApplication))
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
        contentAsString(result) should (include("Liability") and include("<form"))
      }
    }

    "return OK when user has right permissions" in {
      given(fileService.getAttachments(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

      val result = controller(btiCaseWithStatusOPEN, Set(Permission.EDIT_RULING))
        .editRulingDetails("reference")(newFakeGETRequestWithCSRF(fakeApplication))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result = controller(btiCaseWithStatusOPEN, Set.empty)
        .editRulingDetails("reference")(newFakeGETRequestWithCSRF(fakeApplication))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "validateBeforeComplete Ruling" should {
    val btiCaseWithStatusOpenWithDecision = aCase(withBTIApplication, withReference("reference"), withStatus(CaseStatus.OPEN), withDecision())

    "load edit details page when a mandatory field is missing" in {
      val result = controller(btiCaseWithStatusOpenWithDecision, Set(Permission.EDIT_RULING))
        .validateBeforeComplete("reference")(newFakeGETRequestWithCSRF(fakeApplication))

      status(result) shouldBe Status.OK
    }
  }

  "Update Ruling" should {
    val caseWithStatusNEW = aCase(withReference("reference"), withStatus(CaseStatus.NEW))
    val caseWithStatusOPEN = aCase(withReference("reference"), withStatus(CaseStatus.OPEN))
    val liabilityCaseWithStatusOPEN = aCase(withLiabilityApplication(), withReference("reference"), withStatus(CaseStatus.OPEN))
    val updatedCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN))
    val attachment = storedAttachment

    val aValidForm = newFakePOSTRequestWithCSRF(fakeApplication, Map(
      "bindingCommodityCode" -> "",
      "goodsDescription" -> "",
      "methodSearch" -> "",
      "justification" -> "",
      "methodCommercialDenomination" -> "",
      "methodExclusion" -> "",
      "attachments" -> "[]",
      "explanation" -> "")
    )

    "update and redirect for permitted user" when {
      "Case is a BTI" in {
        given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))
        given(fileService.getAttachments(refEq(updatedCase))(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

        val result = await(controller(caseWithStatusOPEN).updateRulingDetails("reference")(aValidForm))
        verify(casesService).updateCase(any[Case])(any[HeaderCarrier])
        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.CaseController.rulingDetails("reference").url)
      }

      "Case is a Liability" in {
        given(commodityCodeConstraints.commodityCodeExistsInUKTradeTariff).willReturn(Constraint[String]("error")( _ =>  Valid))
        given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))

        val result = await(controller(liabilityCaseWithStatusOPEN).updateRulingDetails("reference")(aValidForm))
        verify(casesService).updateCase(any[Case])(any[HeaderCarrier])
        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.LiabilityController.liabilityDetails("reference").url)
      }
    }

    "redirect back to edit ruling on Form Error" when {
      "case is a BTI" in {
        given(fileService.getAttachments(refEq(caseWithStatusOPEN))(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

        val result = controller(caseWithStatusOPEN).updateRulingDetails("reference")(newFakePOSTRequestWithCSRF(fakeApplication))
        verify(casesService, never()).updateCase(any[Case])(any[HeaderCarrier])
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
        contentAsString(result) should include("error-summary")
        contentAsString(result) should (include("Ruling") and include("<form"))
      }

      "case is a Liability" in {
        given(commodityCodeConstraints.commodityCodeExistsInUKTradeTariff).willReturn(Constraint[String]("error")( _ =>  Valid))
        val result = controller(liabilityCaseWithStatusOPEN).updateRulingDetails("reference")(newFakePOSTRequestWithCSRF(fakeApplication))
        verify(casesService, never()).updateCase(any[Case])(any[HeaderCarrier])
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
        contentAsString(result) should include("error-summary")
        contentAsString(result) should (include("Liability") and include("<form"))
      }
    }

    "redirect unauthorised when does not have right permissions" in {
      val result = controller(caseWithStatusOPEN, Set.empty).updateRulingDetails("reference")(aValidForm)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }
}
