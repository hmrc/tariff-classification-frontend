/*
 * Copyright 2023 HM Revenue & Customs
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

import models.forms.v2.LiabilityDetailsForm
import models.forms.{CommodityCodeConstraints, DecisionForm, DecisionFormMapper}
import models.{Case, CaseStatus, Operator, Permission}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify}
import org.scalatest.BeforeAndAfterEach
import play.api.data.validation.{Constraint, Valid}
import play.api.http.Status
import play.api.test.Helpers.{redirectLocation, _}
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.Cases._
import views.html.ruling_details_edit
import views.html.v2.edit_liability_ruling

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RulingControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService             = mock[CasesService]
  private val fileService              = mock[FileStoreService]
  private val mapper                   = mock[DecisionFormMapper]
  private val operator                 = Operator(id = "id")
  private val commodityCodeConstraints = mock[CommodityCodeConstraints]
  private val decisionForm             = new DecisionForm(commodityCodeConstraints)
  private val liabilityDetailsForm     = new LiabilityDetailsForm(commodityCodeConstraints, realAppConfig)

  private lazy val editLiabilityView = injector.instanceOf[edit_liability_ruling]
  private val liability_details_edit = injector.instanceOf[views.html.v2.liability_details_edit]
  private val rulingDetailsEdit      = injector.instanceOf[ruling_details_edit]

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(
      casesService,
      fileService,
      mapper,
      commodityCodeConstraints
    )
  }

  private def controller(c: Case) = new RulingController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = c),
    casesService,
    fileService,
    mapper,
    decisionForm,
    liabilityDetailsForm,
    mcc,
    editLiabilityView,
    liability_details_edit,
    rulingDetailsEdit,
    realAppConfig
  )

  private def controller(c: Case, permission: Set[Permission]) = new RulingController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = c),
    casesService,
    fileService,
    mapper,
    decisionForm,
    liabilityDetailsForm,
    mcc,
    editLiabilityView,
    liability_details_edit,
    rulingDetailsEdit,
    realAppConfig
  )

  "Edit Ruling" should {
    val btiCaseWithStatusOPEN = aCase(withBTIApplication, withReference("reference"), withStatus(CaseStatus.OPEN))
    val liabilityCaseWithStatusOPEN =
      aCase(withLiabilityApplication(), withReference("reference"), withStatus(CaseStatus.OPEN))
    val attachment = storedAttachment

    "return OK and HTML content type" when {
      "Case is an ATaR" in {
        given(fileService.getAttachments(refEq(btiCaseWithStatusOPEN))(any[HeaderCarrier]))
          .willReturn(Future.successful(Seq(attachment)))

        val result = controller(btiCaseWithStatusOPEN).editRulingDetails("reference")(newFakeGETRequestWithCSRF())
        status(result)          shouldBe Status.OK
        contentType(result)     shouldBe Some("text/html")
        charset(result)         shouldBe Some("utf-8")
        contentAsString(result) should (include("Ruling") and include("<form"))
      }
      "Case is a Liability" in {
        given(commodityCodeConstraints.commodityCodeLengthValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        given(commodityCodeConstraints.commodityCodeNumbersValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        given(commodityCodeConstraints.commodityCodeEvenDigitsValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        val result = controller(
          liabilityCaseWithStatusOPEN,
          permission = Set(Permission.EDIT_RULING)
        ).editRulingDetails("reference")(newFakeGETRequestWithCSRF())
        status(result) shouldBe Status.OK
        contentAsString(result) shouldNot (include("edit_liability_decision-heading"))
        contentAsString(result) should (include("case-heading"))
      }

      "Case is an Liability with incorrect permissions" in {
        given(commodityCodeConstraints.commodityCodeLengthValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        given(commodityCodeConstraints.commodityCodeNumbersValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        given(commodityCodeConstraints.commodityCodeEvenDigitsValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        val result = controller(
          liabilityCaseWithStatusOPEN,
          permission = Set.empty[Permission]
        ).editRulingDetails("reference")(newFakeGETRequestWithCSRF())
        status(result)               shouldBe Status.SEE_OTHER
        redirectLocation(result).get should include("unauthorized")
      }
    }

    "return OK when user has right permissions" in {
      given(fileService.getAttachments(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

      val result = controller(btiCaseWithStatusOPEN, Set(Permission.EDIT_RULING))
        .editRulingDetails("reference")(newFakeGETRequestWithCSRF())

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result = controller(btiCaseWithStatusOPEN, Set.empty)
        .editRulingDetails("reference")(newFakeGETRequestWithCSRF())

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "validateBeforeComplete Ruling" should {
    val btiCaseWithStatusOpenWithDecision =
      aCase(withBTIApplication, withReference("reference"), withStatus(CaseStatus.OPEN), withDecision())
    val liabilityCaseWithStatusOpenWithDecision =
      aLiabilityCase(withReference("reference"), withStatus(CaseStatus.COMPLETED), withDecision())
    val attachment = storedAttachment

    "load edit details page when a mandatory field is missing" in {
      given(fileService.getAttachments(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

      val result = controller(btiCaseWithStatusOpenWithDecision, Set(Permission.EDIT_RULING))
        .validateBeforeComplete("reference")(newFakeGETRequestWithCSRF())

      status(result) shouldBe Status.OK
    }

    "load edit ruling page when ruling tab has missing fields that are required to complete a case" in {
      given(commodityCodeConstraints.commodityCodeNonEmpty)
        .willReturn(Constraint[String]("error")(_ => Valid))
      given(commodityCodeConstraints.commodityCodeLengthValid)
        .willReturn(Constraint[String]("error")(_ => Valid))
      given(commodityCodeConstraints.commodityCodeNumbersValid)
        .willReturn(Constraint[String]("error")(_ => Valid))
      given(commodityCodeConstraints.commodityCodeEvenDigitsValid)
        .willReturn(Constraint[String]("error")(_ => Valid))
      val result = controller(Cases.liabilityCaseExample, Set(Permission.EDIT_RULING))
        .validateBeforeComplete("reference")(newFakeGETRequestWithCSRF())

      status(result) shouldBe Status.OK
    }

    "load edit C592 page when C592 tab has missing fields that are required to complete a case" in {
      given(commodityCodeConstraints.commodityCodeNonEmpty)
        .willReturn(Constraint[String]("error")(_ => Valid))
      given(commodityCodeConstraints.commodityCodeLengthValid)
        .willReturn(Constraint[String]("error")(_ => Valid))
      given(commodityCodeConstraints.commodityCodeNumbersValid)
        .willReturn(Constraint[String]("error")(_ => Valid))
      given(commodityCodeConstraints.commodityCodeEvenDigitsValid)
        .willReturn(Constraint[String]("error")(_ => Valid))
      val result = controller(liabilityCaseWithStatusOpenWithDecision, Set(Permission.EDIT_RULING))
        .validateBeforeComplete("reference")(newFakeGETRequestWithCSRF())

      status(result) shouldBe Status.OK
    }
  }

  "Update Ruling" should {
    val caseWithStatusOPEN = aCase(withReference("reference"), withStatus(CaseStatus.OPEN))
    val liabilityCaseWithStatusOPEN =
      aCase(withLiabilityApplication(), withReference("reference"), withStatus(CaseStatus.OPEN))
    val updatedCase = aCase(withReference("reference"), withStatus(CaseStatus.OPEN))
    val attachment  = storedAttachment

    val aValidForm = newFakePOSTRequestWithCSRF(
      Map(
        "bindingCommodityCode"         -> "",
        "goodsDescription"             -> "",
        "methodSearch"                 -> "",
        "justification"                -> "",
        "methodCommercialDenomination" -> "",
        "methodExclusion"              -> "",
        "attachments"                  -> "[]",
        "explanation"                  -> "",
        "expiryDate.day"               -> "2",
        "expiryDate.month"             -> "2",
        "expiryDate.year"              -> "2020",
        "explicitEndDate"              -> "false"
      )
    )

    "update and redirect for permitted user" when {
      "Case is an ATaR" in {
        given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier]))
          .willReturn(Future.successful(updatedCase))
        given(fileService.getAttachments(refEq(updatedCase))(any[HeaderCarrier]))
          .willReturn(Future.successful(Seq(attachment)))

        val result = await(controller(caseWithStatusOPEN).updateRulingDetails("reference")(aValidForm))
        verify(casesService).updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])
        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.AtarController.displayAtar("reference").withFragment(Tab.RULING_TAB.name).path
        )
      }
    }

    "redirect back to edit ruling on Form Error" when {
      "case is an ATaR" in {
        given(fileService.getAttachments(refEq(caseWithStatusOPEN))(any[HeaderCarrier]))
          .willReturn(Future.successful(Seq(attachment)))

        val result = controller(caseWithStatusOPEN).updateRulingDetails("reference")(newFakePOSTRequestWithCSRF())
        verify(casesService, never()).updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])
        status(result)          shouldBe Status.OK
        contentType(result)     shouldBe Some("text/html")
        charset(result)         shouldBe Some("utf-8")
        contentAsString(result) should include("error-summary")
        contentAsString(result) should (include("Ruling") and include("<form"))
      }

      "case is a Liability" in {
        given(commodityCodeConstraints.commodityCodeLengthValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        given(commodityCodeConstraints.commodityCodeNumbersValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        given(commodityCodeConstraints.commodityCodeEvenDigitsValid)
          .willReturn(Constraint[String]("error")(_ => Valid))
        val result =
          controller(liabilityCaseWithStatusOPEN).updateRulingDetails("reference")(newFakePOSTRequestWithCSRF())
        verify(casesService, never()).updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])
        status(result)          shouldBe Status.OK
        contentType(result)     shouldBe Some("text/html")
        charset(result)         shouldBe Some("utf-8")
        contentAsString(result) should include("error-summary")
        contentAsString(result) should (include("Liability") and include("<form"))
      }
    }

    "redirect unauthorised when does not have right permissions" in {
      val result = controller(caseWithStatusOPEN, Set.empty).updateRulingDetails("reference")(aValidForm)

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }
}
