/*
 * Copyright 2025 HM Revenue & Customs
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

import models._
import models.forms.v2.LiabilityDetailsForm
import models.forms.{CommodityCodeConstraints, DecisionForm}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.test.Helpers._
import services.{CasesService, CommodityCodeService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._
import views.html.{complete_case, confirm_complete_case}

import java.time.Instant
import scala.concurrent.Future.successful

class CompleteCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService         = mock[CasesService]
  private val operator             = Operator(id = "id")
  private val commodityCodeService = mock[CommodityCodeService]
  private val decisionForm         = new DecisionForm()
  private val liabilityDetailsForm =
    new LiabilityDetailsForm(new CommodityCodeConstraints(), realAppConfig)

  private val confirmCompleteCase = injector.instanceOf[confirm_complete_case]
  private val completeCase        = injector.instanceOf[complete_case]

  private val completeDecision = Decision(
    bindingCommodityCode = "040900",
    effectiveEndDate = Some(Instant.now),
    justification = "justification-content",
    goodsDescription = "goods-description",
    methodSearch = Some("method-to-search"),
    explanation = Some("explanation")
  )

  private val inCompleteDecision = Decision(bindingCommodityCode = "", justification = "", goodsDescription = "")

  private val validCaseWithStatusOPEN =
    btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN, decision = Some(completeDecision))
  private val caseWithStatusCOMPLETED = btiCaseExample.copy(reference = "reference", status = CaseStatus.COMPLETED)
  private val caseWithoutDecision =
    btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN, decision = None)
  private val caseWithIncompleteDecision =
    btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN, decision = Some(inCompleteDecision))

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def getController(requestCase: Case): CompleteCaseController =
    new CompleteCaseController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
      casesService,
      decisionForm,
      liabilityDetailsForm,
      mcc,
      confirmCompleteCase,
      completeCase,
      realAppConfig
    )(executionContext)

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new CompleteCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      decisionForm,
      liabilityDetailsForm,
      mcc,
      confirmCompleteCase,
      completeCase,
      realAppConfig
    )(executionContext)

  "Complete Case" should {

    when(commodityCodeService.find(any[String])).thenReturn(Some(CommodityCode("code")))

    "return OK and HTML content type" when {
      "Case is a valid BTI" in {
        val result: Result =
          await(getController(validCaseWithStatusOPEN).completeCase("reference")(newFakeGETRequestWithCSRF()))

        status(result)        shouldBe Status.OK
        contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
        charsetOf(result)     shouldBe Some("utf-8")
        bodyOf(result)          should include("Are you sure you want to complete the Laptop case?")
      }

      "Case is a valid Liability" in {
        val c = aCase(
          withReference("reference"),
          withStatus(CaseStatus.OPEN),
          liabilityApplicationWithC592(),
          withDecision(bindingCommodityCode = "040900")
        )

        when(casesService.completeCase(refEq(c), any[Operator])(any[HeaderCarrier], any[Messages]))
          .thenReturn(successful(caseWithStatusCOMPLETED))

        val result: Result = await(getController(c).completeCase("reference")(newFakeGETRequestWithCSRF()))

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/complete/confirmation")
      }
    }

    "redirect to default page (validateBeforeComplete) for cases without a decision" in {
      val result: Result =
        await(getController(caseWithoutDecision).completeCase("reference")(newFakeGETRequestWithCSRF()))

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.RulingController.validateBeforeComplete("reference").url)
    }

    "redirect to default page (validateBeforeComplete) for Liability case with incomplete decision" in {
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication(),
        withDecision(goodsDescription = "")
      )

      val result: Result = await(getController(c).completeCase("reference")(newFakePOSTRequestWithCSRF()))

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.RulingController.validateBeforeComplete("reference").url)
    }

    "redirect to default page (validateBeforeComplete) for Liability case with incomplete application" in {
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication(goodName = None),
        withDecision()
      )

      val result: Result = await(getController(c).completeCase("reference")(newFakePOSTRequestWithCSRF()))

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.RulingController.validateBeforeComplete("reference").url)
    }

    "return OK when user has right permissions" in {
      val result: Result = await(
        controller(validCaseWithStatusOPEN, Set(Permission.COMPLETE_CASE))
          .completeCase("reference")(newFakeGETRequestWithCSRF())
      )

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(validCaseWithStatusOPEN, Set.empty)
          .completeCase("reference")(newFakeGETRequestWithCSRF())
      )

      status(result)             shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Confirm Complete Case" should {

    "return OK and HTML content type" in {
      when(casesService.completeCase(refEq(validCaseWithStatusOPEN), any[Operator])(any[HeaderCarrier], any[Messages]))
        .thenReturn(successful(caseWithStatusCOMPLETED))

      val result: Result =
        await(
          getController(validCaseWithStatusOPEN).postCompleteCase("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("state" -> "true")
          )
        )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/complete/confirmation")
    }

    "redirect to view case when 'No' selected" in {
      when(casesService.completeCase(refEq(validCaseWithStatusOPEN), any[Operator])(any[HeaderCarrier], any[Messages]))
        .thenReturn(successful(caseWithStatusCOMPLETED))

      val result: Result =
        await(
          getController(validCaseWithStatusOPEN).postCompleteCase("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("state" -> "false")
          )
        )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/ruling")
    }

    "return form error when no option selected" in {
      when(casesService.completeCase(refEq(validCaseWithStatusOPEN), any[Operator])(any[HeaderCarrier], any[Messages]))
        .thenReturn(successful(caseWithStatusCOMPLETED))

      val result: Result =
        await(
          getController(validCaseWithStatusOPEN).postCompleteCase("reference")(
            newFakePOSTRequestWithCSRF()
          )
        )

      status(result) shouldBe Status.OK
      bodyOf(result)   should include("Select yes if you want to complete this case")
    }

    "redirect to default page for non OPEN statuses" in {
      val result: Result =
        await(
          getController(caseWithStatusCOMPLETED).postCompleteCase("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("state" -> "true")
          )
        )

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to default page for case without decision" in {

      val result: Result =
        await(
          getController(caseWithoutDecision).postCompleteCase("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("state" -> "true")
          )
        )

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to default page for BTI case with incomplete decision" in {
      val result: Result =
        await(
          getController(caseWithIncompleteDecision).postCompleteCase("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("state" -> "true")
          )
        )

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to default page for Liability case with incomplete decision" in {
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication(),
        withDecision(goodsDescription = "")
      )

      val result: Result = await(
        getController(c).postCompleteCase("reference")(
          newFakePOSTRequestWithCSRF()
            .withFormUrlEncodedBody("state" -> "true")
        )
      )

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to default page for Liability case with incomplete application" in {
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication(goodName = None),
        withDecision()
      )

      val result: Result = await(
        getController(c).postCompleteCase("reference")(
          newFakePOSTRequestWithCSRF()
            .withFormUrlEncodedBody("state" -> "true")
        )
      )

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }

    "return OK when user has right permissions" in {
      when(casesService.completeCase(any[Case], any[Operator])(any[HeaderCarrier], any[Messages]))
        .thenReturn(successful(caseWithStatusCOMPLETED))

      val result: Result = await(
        controller(validCaseWithStatusOPEN, Set(Permission.COMPLETE_CASE))
          .postCompleteCase("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("state" -> "true"))
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/complete/confirmation")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(validCaseWithStatusOPEN, Set.empty)
          .postCompleteCase("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("state" -> "true"))
      )

      status(result)             shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "View Confirm page for a complete case" should {

    "return OK and HTML content type" in {
      val result: Result =
        await(getController(caseWithStatusCOMPLETED).confirmCompleteCase("reference")(newFakeGETRequestWithCSRF()))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include(messages("complete_case.completed.header", "Laptop", "ATaR"))
    }

    "redirect to a default page if the status is not right" in {
      val result: Result =
        await(getController(validCaseWithStatusOPEN).confirmCompleteCase("reference")(newFakeGETRequestWithCSRF()))

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }
  }

}
