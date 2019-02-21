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

import java.time.Clock

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{MimeTypes, Status}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.Result
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.{failed, successful}

class CompleteCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val completeDecision = Decision(
    bindingCommodityCode = "123456789",
    justification = "justification-content",
    goodsDescription = "goods-description",
    methodSearch = Some("method-to-search"))

  private val inCompleteDecision = Decision(bindingCommodityCode = "", justification = "", goodsDescription = "")

  private val validCaseWithStatusOPEN = Cases.btiCaseExample.copy(status = CaseStatus.OPEN, decision = Some(completeDecision))
  private val caseWithStatusCOMPLETED = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)
  private val caseWithoutDecision = Cases.btiCaseExample.copy(status = CaseStatus.OPEN, decision = None)
  private val caseWithoutIncompleteDecision = Cases.btiCaseExample.copy(status = CaseStatus.OPEN, decision = Some(inCompleteDecision))

  private implicit val mat: Materializer = app.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val controller = new CompleteCaseController(new SuccessfulAuthenticatedAction(operator), casesService, messageApi, appConfig)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  "Complete Case" should {

    "return OK and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(validCaseWithStatusOPEN)))

      val result: Result = await(controller.completeCase("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Complete this case")
    }

    "redirect to Application Details for non OPEN statuses" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusCOMPLETED)))

      val result: Result = await(controller.completeCase("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "redirect to Application Details for cases without a decision" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithoutDecision)))

      val result: Result = await(controller.completeCase("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "return Not Found and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(None))

      val result: Result = await(controller.completeCase("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("We could not find a Case with reference")
    }

  }

  "Confirm Complete Case" should {

    "return OK and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(validCaseWithStatusOPEN)))
      when(casesService.completeCase(refEq(validCaseWithStatusOPEN), refEq(operator), any[Clock])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusCOMPLETED))

      val result: Result = await(controller.confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This case has been completed")
    }

    "redirect to Application Details for non OPEN statuses" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusCOMPLETED)))

      val result: Result = await(controller.confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(app)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "redirect to Application Details for case without decision" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithoutDecision)))

      val result: Result = await(controller.confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(app)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "redirect to Application Details for case with incomplete decision" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithoutIncompleteDecision)))

      val result: Result = await(controller.confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(app)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "return Not Found and HTML content type on missing Case" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(None))

      val result: Result = await(controller.confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("We could not find a Case with reference")
    }

    "propagate the error in case the CaseService fails to release the case" in {
      val error = new IllegalStateException("expected error")

      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(failed(error))

      val caught = intercept[error.type] {
        await(controller.confirmCompleteCase("reference")(newFakePOSTRequestWithCSRF(app)))
      }
      caught shouldBe error
    }
  }

}
