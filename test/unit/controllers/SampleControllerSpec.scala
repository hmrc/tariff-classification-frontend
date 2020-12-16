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

import models.EventType.EventType
import models.SampleStatus.SampleStatus
import models.{Permission, _}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, EventsService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SampleControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService  = mock[CasesService]
  private val eventsService = mock[EventsService]
  private val operator      = Operator(id = "id")

  private def controller(requestCase: Case) = new SampleController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    eventsService,
    mcc,
    appConfWithLiabilityToggleOff
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new SampleController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    eventsService,
    mcc,
    appConfWithLiabilityToggleOff
  )

  private def controllerV2(requestCase: Case, permission: Set[Permission]) = new SampleController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    eventsService,
    mcc,
    realAppConfig
  )
  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Sample controller" should {

    "return 200 OK and HTML content type - When retrieving samples for case" in {

      when(
        eventsService.getFilteredEvents(any[String], any[Pagination], any[Option[Set[EventType]]])(any[HeaderCarrier])
      ).thenReturn(Future.successful(Paged.empty[Event]))

      val c = aCase(withStatus(CaseStatus.OPEN), withBTIDetails(sampleToBeProvided = true))

      val result = await(controller(c).sampleDetails("reference")(fakeRequest))

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("sample-status-heading")

      verify(eventsService).getFilteredEvents(
        refEq(c.reference),
        refEq(NoPagination()),
        refEq(Some(EventType.sampleEvents))
      )(any[HeaderCarrier])
    }

  }

  "Sample - Choose Status" should {

    "return 200 OK and HTML content type - For Case" in {
      val c = aCase(withStatus(CaseStatus.OPEN), withDecision())

      val result = await(controller(c).chooseStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("change_sample_status-heading")
    }

    "return OK when user has right permissions" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result =
        await(controller(c, Set(Permission.EDIT_SAMPLE)).chooseStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
    }

    "return OK when user has right permissions for liability" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result =
        await(controller(c, Set(Permission.EDIT_SAMPLE)).chooseStatus("reference", Some("liability"))(newFakeGETRequestWithCSRF(app)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(controller(c, Set.empty).chooseStatus("reference")(newFakeGETRequestWithCSRF(app)))

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
          .updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("status" -> "DESTROYED"))
      )

      verify(casesService)
        .updateSampleStatus(refEq(c), refEq(Some(SampleStatus.DESTROYED)), any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/sample")
    }

    "redirect for unchanged status" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.CANCELLED), withDecision())

      val result = await(
        controller(c).updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("status" -> ""))
      )

      verify(casesService, never())
        .updateSampleStatus(any[Case], any[Option[SampleStatus]], any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/sample")
    }

    "when error form re-displays with error message" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

      val result = await(
        controller(c).updateStatus("reference")(
          newFakePOSTRequestWithCSRF(app)
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
          .updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("status" -> "AWAITING"))
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/sample")
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(
        controller(c, Set.empty)
          .updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("status" -> "AWAITING"))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "update & redirect when isV2Liability is set to true" in {
      val c = aLiabilityCase(withReference("reference"), withSampleStatus(Some(SampleStatus.AWAITING)), withDecision())

      given(casesService.updateSampleStatus(refEq(c), any[Option[SampleStatus]], any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(c))

      val result = await(
        controllerV2(c, Set(Permission.EDIT_SAMPLE))
          .updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("status" -> ""))
      )

      verify(casesService).updateSampleStatus(refEq(c), refEq(None), any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/v2/reference/liability#sample_status_tab")
    }

  }

}
