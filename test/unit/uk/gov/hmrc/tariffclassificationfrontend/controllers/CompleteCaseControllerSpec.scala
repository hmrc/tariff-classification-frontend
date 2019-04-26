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

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import play.api.http.{MimeTypes, Status}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.Result
import play.api.test.Helpers.{redirectLocation, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{CommodityCodeConstraints, DecisionForm}
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, CommodityCodeService}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.successful

class CompleteCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]
  private val commodityCodeService = mock[CommodityCodeService]
  private val decisionForm = new DecisionForm(new CommodityCodeConstraints(commodityCodeService))

  private val completeDecision = Decision(
    bindingCommodityCode = "040900",
    justification = "justification-content",
    goodsDescription = "goods-description",
    methodSearch = Some("method-to-search"),
    explanation = Some("explanation"))

  private val inCompleteDecision = Decision(bindingCommodityCode = "", justification = "", goodsDescription = "")

  private val validCaseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN, decision = Some(completeDecision))
  private val caseWithStatusCOMPLETED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.COMPLETED)
  private val caseWithoutDecision = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN, decision = None)
  private val caseWithoutIncompleteDecision = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN, decision = Some(inCompleteDecision))

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def getController(requestCase: Case): CompleteCaseController = {
    new CompleteCaseController(new SuccessfulRequestActions(operator, c = requestCase), casesService, decisionForm, messageApi, appConfig)
  }

  private def controller(requestCase: Case, permission: Set[Permission]) = new CompleteCaseController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, decisionForm, messageApi, appConfig)


  "Complete Case" should {

    when(commodityCodeService.checkIfCodeExists(any())).thenReturn(true)

    "return OK and HTML content type" in {

      val result: Result = await(getController(validCaseWithStatusOPEN).completeCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Complete this case")
    }

    "redirect to Application Details for non OPEN statuses" in {
      val result: Result = await(getController(caseWithStatusCOMPLETED).completeCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "redirect to Application Details for cases without a decision" in {
      val result: Result = await(getController(caseWithoutDecision).completeCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "return OK when user has right permissions" in {
      val result: Result = await(controller(validCaseWithStatusOPEN, Set(Permission.COMPLETE_CASE))
        .completeCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(validCaseWithStatusOPEN, Set.empty)
          .completeCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Confirm Complete Case" should {

    "return OK and HTML content type" in {
      when(casesService.completeCase(refEq(validCaseWithStatusOPEN), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusCOMPLETED))

      val result: Result = await(getController(validCaseWithStatusOPEN).confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This case has been completed")
    }

    "redirect to Application Details for non OPEN statuses" in {
      val result: Result = await(getController(caseWithStatusCOMPLETED).confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "redirect to Application Details for case without decision" in {

      val result: Result = await(getController(caseWithoutDecision).confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "redirect to Application Details for case with incomplete decision" in {
      val result: Result = await(getController(caseWithoutIncompleteDecision).confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "return OK when user has right permissions" in {
      when(casesService.completeCase(any[Case], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusCOMPLETED))

      val result: Result = await(controller(validCaseWithStatusOPEN, Set(Permission.COMPLETE_CASE))
        .confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(validCaseWithStatusOPEN, Set.empty)
        .confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

}
