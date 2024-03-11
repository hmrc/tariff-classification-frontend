/*
 * Copyright 2024 HM Revenue & Customs
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

import models.SampleStatus.SampleStatus
import models._
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
import views.html.{change_correspondence_sending_sample, change_liablity_sending_sample, change_sample_status}

import scala.concurrent.Future

class SampleControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator(id = "id")

  private val changeLiabilitySendingSample      = app.injector.instanceOf[change_liablity_sending_sample]
  private val changeSampleStatus                = app.injector.instanceOf[change_sample_status]
  private val changeCorrespondenceSendingSample = app.injector.instanceOf[change_correspondence_sending_sample]

  private def controller(requestCase: Case) = new SampleController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    mcc,
    realAppConfig,
    changeLiabilitySendingSample,
    changeSampleStatus,
    changeCorrespondenceSendingSample
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new SampleController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    mcc,
    realAppConfig,
    changeLiabilitySendingSample,
    changeSampleStatus,
    changeCorrespondenceSendingSample
  )

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Sample - Choose Status" should {

    "return 200 OK and HTML content type - For Case" in {
      val c = aCase(withStatus(CaseStatus.OPEN), withDecision())

      val result = await(controller(c).chooseStatus("reference")(newFakeGETRequestWithCSRF()))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)      should include("<h1 class=\"govuk-heading-xl\" id=\"heading\">")
    }

    "return OK when user has right permissions" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result =
        await(controller(c, Set(Permission.EDIT_SAMPLE)).chooseStatus("reference")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
    }

    "return OK when user has right permissions for liability" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result =
        await(
          controller(c, Set(Permission.EDIT_SAMPLE))
            .chooseStatus("reference", Some("liability"))(newFakeGETRequestWithCSRF())
        )

      status(result) shouldBe Status.OK
    }

    "return OK when user has right permissions for correspondence" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result =
        await(
          controller(c, Set(Permission.EDIT_SAMPLE))
            .chooseStatus("reference", Some("correspondence"))(newFakeGETRequestWithCSRF())
        )

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(controller(c, Set.empty).chooseStatus("reference")(newFakeGETRequestWithCSRF()))

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Sample - Submit Status" should {

    "update & redirect - For Case" in {
      val c = aCase(withReference("reference"), withDecision())

      given(casesService.updateSampleStatus(refEq(c), any[Option[SampleStatus]], any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(c))

      val result = await(
        controller(c)
          .updateStatus("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("status" -> "DESTROYED"))
      )

      verify(casesService)
        .updateSampleStatus(refEq(c), refEq(Some(SampleStatus.DESTROYED)), any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.sampleDetails("reference").path)
    }

    "redirect for unchanged status" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.CANCELLED), withDecision())

      val result = await(
        controller(c).updateStatus("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("status" -> ""))
      )

      verify(casesService, never())
        .updateSampleStatus(any[Case], any[Option[SampleStatus]], any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.sampleDetails("reference").path)
    }

    "when error form re-displays with error message" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

      val result = await(
        controller(c).updateStatus("reference")(
          newFakePOSTRequestWithCSRF()
            .withFormUrlEncodedBody("status" -> "WRONG_STATUS")
        )
      )

      verify(casesService, never())
        .updateSampleStatus(any[Case], any[Option[SampleStatus]], any[Operator])(any[HeaderCarrier])

      status(result)          shouldBe Status.OK
      contentAsString(result) should include("error-message-status-input")
    }

    "return OK when user has right permissions" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.COMPLETED), withDecision())

      given(casesService.updateSampleStatus(any[Case], any[Option[SampleStatus]], any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(c))

      val result = await(
        controller(c, Set(Permission.EDIT_SAMPLE))
          .updateStatus("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("status" -> "AWAITING"))
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.sampleDetails("reference").path)
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(
        controller(c, Set.empty)
          .updateStatus("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("status" -> "AWAITING"))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

}
