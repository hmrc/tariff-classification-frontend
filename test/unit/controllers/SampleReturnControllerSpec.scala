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

import models.SampleReturn.SampleReturn
import models.{Permission, SampleReturn, _}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._

import scala.concurrent.Future

class SampleReturnControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private def controller(requestCase: Case) = new SampleReturnController(
    new SuccessfulRequestActions(defaultPlayBodyParsers, operator, c = requestCase), casesService, mcc, appConfWithLiabilityToggleOff
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new SampleReturnController(
    new RequestActionsWithPermissions(defaultPlayBodyParsers, permission, c = requestCase), casesService, mcc, appConfWithLiabilityToggleOff
  )

  private def controllerV2(requestCase: Case, permission: Set[Permission]) = new SampleReturnController(
    new RequestActionsWithPermissions(defaultPlayBodyParsers, permission, c = requestCase), casesService, mcc, realAppConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Sample - Choose Return" should {

    "return 200 OK and HTML" in {
      val c = aCase(withStatus(CaseStatus.OPEN), withDecision())

      val result = await(controller(c).chooseStatus("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("Should the sample be returned?")
    }

    "return OK when user has right permissions" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(controller(c, Set(Permission.EDIT_SAMPLE)).chooseStatus("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(controller(c, Set.empty).chooseStatus("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Sample - Submit Return" should {

    "update & redirect" in {
      val c = aCase(withReference("reference"), withDecision())

      given(casesService.updateSampleReturn(refEq(c), any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(c))

      val result = await(controller(c).updateStatus("reference")(newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("return" -> "YES")))

      verify(casesService).updateSampleReturn(refEq(c), refEq(Some(SampleReturn.YES)), any[Operator])(any[HeaderCarrier])

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/sample")
    }

    "redirect for unchanged status" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.CANCELLED), withDecision())

      val result = await(controller(c).updateStatus("reference")(newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("return" -> "")))

      verify(casesService, never()).updateSampleReturn(any[Case], any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier])

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/sample")
    }

    "when error form re-displays with error message" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

      val result = await(controller(c).updateStatus("reference")(newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("return" -> "WRONG_STATUS")))

      verify(casesService, never()).updateSampleReturn(any[Case], any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier])

      status(result) shouldBe Status.OK
      contentAsString(result) should include("error-message-return-input")
    }

    "return OK when user has right permissions" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.COMPLETED), withDecision())

      given(casesService.updateSampleReturn(any[Case], any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(c))

      val result = await(controller(c, Set(Permission.EDIT_SAMPLE)).updateStatus("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("return" -> "NO")))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/sample")
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(controller(c, Set.empty).updateStatus("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("return" -> "NO")))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "update & redirect when isV2Liability is set to true" in {
      val c = aLiabilityCase(withReference("reference"), withDecision())

      given(casesService.updateSampleReturn(refEq(c), any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(c))

      val result = await(controllerV2(c, Set(Permission.EDIT_SAMPLE)).updateStatus("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("return" -> "YES")))

      verify(casesService).updateSampleReturn(refEq(c), refEq(Some(SampleReturn.YES)), any[Operator])(any[HeaderCarrier])

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/v2/reference/liability#sample_status_tab")
    }

  }

}
