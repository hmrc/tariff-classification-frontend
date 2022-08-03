/*
 * Copyright 2022 HM Revenue & Customs
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
import views.html.change_sample_return

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SampleReturnControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator(id = "id")
  private val changeSampleReturn = app.injector.instanceOf[change_sample_return]
  private def controller(requestCase: Case) = new SampleReturnController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    changeSampleReturn,
    mcc,
    realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new SampleReturnController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    changeSampleReturn,
    mcc,
    realAppConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Sample - Choose Return" should {

    "return 200 OK and HTML" in {
      val c = aCase(withStatus(CaseStatus.OPEN), withDecision())

      val result = await(controller(c).chooseStatus("reference")(newFakeGETRequestWithCSRF()))

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      bodyOf(result) should include("Should this sample be returned?")
    }

    "return OK when user has right permissions" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result =
        await(controller(c, Set(Permission.EDIT_SAMPLE)).chooseStatus("reference")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(controller(c, Set.empty).chooseStatus("reference")(newFakeGETRequestWithCSRF()))

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Sample - Submit Return" should {

    "update & redirect" in {
      val c = aCase(withReference("reference"), withDecision())

      given(casesService.updateSampleReturn(refEq(c), any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(c))

      val result = await(
        controller(c)
          .updateStatus("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("return" -> "YES"))
      )

      verify(casesService)
        .updateSampleReturn(refEq(c), refEq(Some(SampleReturn.YES)), any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.sampleDetails("reference").path)
    }

    "redirect for unchanged status" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.CANCELLED), withDecision())

      val result = await(
        controller(c).updateStatus("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("return" -> ""))
      )

      verify(casesService, never())
        .updateSampleReturn(any[Case], any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.sampleDetails("reference").path)
    }

    "when error form re-displays with error message" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

      val result = await(
        controller(c).updateStatus("reference")(
          newFakePOSTRequestWithCSRF()
            .withFormUrlEncodedBody("return" -> "WRONG_STATUS")
        )
      )

      verify(casesService, never())
        .updateSampleReturn(any[Case], any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier])

      status(result)          shouldBe Status.OK
      contentAsString(result) should include("error-message-return-input")
    }

    "return OK when user has right permissions" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.COMPLETED), withDecision())

      given(casesService.updateSampleReturn(any[Case], any[Option[SampleReturn]], any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(c))

      val result = await(
        controller(c, Set(Permission.EDIT_SAMPLE))
          .updateStatus("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("return" -> "NO"))
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.sampleDetails("reference").path)
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(withReference("reference"), withStatus(CaseStatus.COMPLETED), withDecision())

      val result = await(
        controller(c, Set.empty)
          .updateStatus("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("return" -> "NO"))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

}
