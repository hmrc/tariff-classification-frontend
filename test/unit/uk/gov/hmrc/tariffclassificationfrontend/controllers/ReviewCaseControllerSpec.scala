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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.ReviewStatus.ReviewStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Operator, ReviewStatus}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future

class ReviewCaseControllerSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons with BeforeAndAfterEach {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val controller = new ReviewCaseController(new SuccessfulAuthenticatedAction(operator), casesService, messageApi, appConfig)

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Case Review - Choose Status" should {

    "return 200 OK and HTML content type - For CANCELLED Case" in {
      val aCase = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))

      val result = await(controller.chooseReviewStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("change_review_status-heading")
    }

    "return 200 OK and HTML content type - For COMPLETED Case" in {
      val aCase = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)

      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))

      val result = await(controller.chooseReviewStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("change_review_status-heading")
    }

    "redirect for other status" in {
      val aCase = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))

      val result = await(controller.chooseReviewStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference")
    }

    "return 404 Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = await(controller.chooseReviewStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }

  "Case Review - Submit Status" should {

    "update & redirect - For CANCELLED Case" in {
      val aCase = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(casesService.updateReviewStatus(refEq(aCase), any[Option[ReviewStatus]], any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(aCase))

      val result = await(controller.updateReviewStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("status" -> "IN_PROGRESS")))

      verify(casesService).updateReviewStatus(refEq(aCase), refEq(Some(ReviewStatus.IN_PROGRESS)), any[Operator])(any[HeaderCarrier])

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/1/appeal")
    }

    "update & redirect - For COMPLETED Case" in {
      val aCase = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)

      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(casesService.updateReviewStatus(refEq(aCase), any[Option[ReviewStatus]], any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(aCase))

      val result = await(controller.updateReviewStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("status" -> "IN_PROGRESS")))

      verify(casesService).updateReviewStatus(refEq(aCase), refEq(Some(ReviewStatus.IN_PROGRESS)), any[Operator])(any[HeaderCarrier])

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/1/appeal")
    }

    "redirect for unchanged status" in {
      val aCase = Cases.btiCaseExample.copy(decision = None)

      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))

      val result = await(controller.updateReviewStatus("reference")(newFakePOSTRequestWithCSRF(app)))

      verify(casesService, never()).updateReviewStatus(any[Case], any[Option[ReviewStatus]], any[Operator])(any[HeaderCarrier])

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference")
    }

    "redirect for other status" in {
      val aCase = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(casesService.updateReviewStatus(refEq(aCase), any[Option[ReviewStatus]], any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(aCase))

      val result = await(controller.updateReviewStatus("reference")(newFakePOSTRequestWithCSRF(app)))

      verify(casesService, never()).updateReviewStatus(any[Case], any[Option[ReviewStatus]], any[Operator])(any[HeaderCarrier])

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference")
    }

    "return 404 Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = await(controller.updateReviewStatus("reference")(newFakePOSTRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }

}
