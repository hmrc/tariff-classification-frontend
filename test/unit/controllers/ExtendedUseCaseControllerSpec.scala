/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{Permission, _}
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
import scala.concurrent.ExecutionContext.Implicits.global

class ExtendedUseCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator(id = "id")

  private def controller(requestCase: Case) = new ExtendedUseCaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    mcc,
    realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new ExtendedUseCaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    mcc,
    realAppConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Case Extended Use - Choose Status" should {

    "return OK when user has right permissions" in {
      val c =
        aCase(withStatus(CaseStatus.CANCELLED), withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED))))

      val result =
        await(controller(c, Set(Permission.EXTENDED_USE)).chooseStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("Do you want to extend the use of this case ruling?")
    }

    "redirect unauthorised when does not have right permissions" in {
      val c =
        aCase(withStatus(CaseStatus.CANCELLED), withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED))))

      val result = await(controller(c, Set.empty).chooseStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "Case Extended Use - Submit Status" should {

    "update & redirect - For CANCELLED Case" in {
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.CANCELLED),
        withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true)))
      )

      given(casesService.updateExtendedUseStatus(refEq(c), any[Boolean], any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(c))

      val result = await(
        controller(c)
          .updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("state" -> "false"))
      )

      verify(casesService).updateExtendedUseStatus(refEq(c), refEq(false), any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.AppealCaseController.appealDetails("reference").url)
    }

    "redirect for unchanged status" in {
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.CANCELLED),
        withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true)))
      )

      val result = await(
        controller(c)
          .updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("state" -> "true"))
      )

      verify(casesService, never()).updateExtendedUseStatus(any[Case], any[Boolean], any[Operator])(any[HeaderCarrier])

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.AppealCaseController.appealDetails("reference").url)
    }

    "return OK when user has right permissions" in {
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.CANCELLED),
        withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true)))
      )

      given(casesService.updateExtendedUseStatus(refEq(c), any[Boolean], any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(c))

      val result = await(
        controller(c, Set(Permission.EXTENDED_USE))
          .updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("state" -> "false"))
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.AppealCaseController.appealDetails("reference").url)
    }

    "redirect unauthorised when does not have right permissions" in {
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.CANCELLED),
        withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true)))
      )

      val result = await(
        controller(c, Set.empty)
          .updateStatus("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("state" -> "false"))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

}
