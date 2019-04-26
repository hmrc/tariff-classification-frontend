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
import uk.gov.hmrc.tariffclassificationfrontend.models.CancelReason.CancelReason
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases
import uk.gov.tariffclassificationfrontend.utils.Cases.btiCaseWithExpiredRuling

import scala.concurrent.Future.successful

class CancelRulingControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
  private val caseWithStatusCOMPLETED = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)
  private val caseWithStatusCANCELLED = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

  private val rulingDetailsUrl = "/tariff-classification/cases/reference/ruling"
  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(requestCase: Case) = new CancelRulingController(
    new SuccessfulRequestActions(operator, c = requestCase), casesService, messageApi, appConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new CancelRulingController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, messageApi, appConfig)


  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  "Cancel Ruling" should {

    "return OK and HTML content type" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED).cancelRuling("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Cancel the ruling")
    }

    "redirect to Ruling Details for non COMPLETED statuses" in {
      val result: Result = await(controller(caseWithStatusOPEN).cancelRuling("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(rulingDetailsUrl)
    }

    "redirect to Ruling Details for expired rulings" in {

      val result: Result = await(controller(btiCaseWithExpiredRuling).cancelRuling("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(rulingDetailsUrl)
    }

    "return OK when user has right permissions" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED, Set(Permission.CANCEL_CASE))
        .cancelRuling("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED, Set.empty)
        .cancelRuling("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "Confirm Cancel a Ruling" should {

    "return OK and HTML content type" in {
      when(casesService.cancelRuling(refEq(caseWithStatusCOMPLETED), refEq(CancelReason.ANNULLED), refEq(operator))
      (any[HeaderCarrier])).thenReturn(successful(caseWithStatusCANCELLED))

      val result: Result = await(controller(caseWithStatusCOMPLETED).confirmCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("reason" -> "ANNULLED")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("The ruling has been cancelled")
    }

    "display required field when failing to submit reason" in {
      when(casesService.cancelRuling(refEq(caseWithStatusCOMPLETED), refEq(CancelReason.ANNULLED), refEq(operator))
      (any[HeaderCarrier])).thenReturn(successful(caseWithStatusCANCELLED))

      val result: Result = await(controller(caseWithStatusCOMPLETED).confirmCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This field is required")
    }

    "redirect to Ruling Details for non COMPLETED statuses" in {
      val result: Result = await(controller(caseWithStatusOPEN).confirmCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(rulingDetailsUrl)
    }

    "redirect to Ruling Details for expired rulings" in {
      val result: Result = await(controller(btiCaseWithExpiredRuling).confirmCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(rulingDetailsUrl)
    }

    "return OK when user has right permissions" in {
      when(casesService.cancelRuling(any[Case], any[CancelReason], any[Operator])
      (any[HeaderCarrier])).thenReturn(successful(caseWithStatusCANCELLED))

      val result: Result = await(controller(caseWithStatusCOMPLETED, Set(Permission.CANCEL_CASE))
        .confirmCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("reason" -> "ANNULLED")))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED, Set.empty)
        .confirmCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("reason" -> "ANNULLED")))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

}
