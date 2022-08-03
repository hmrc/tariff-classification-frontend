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

import models.{Case, CaseStatus, Operator, Permission}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers.{redirectLocation, _}
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class ReopenCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator(id = "id")

  private val btiCaseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val btiCaseWithStatusREFERRED =
    Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.REFERRED)
  private val btiCaseWithStatusSUSPENDED =
    Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.SUSPENDED)

  private val liabilityCaseWithStatusOpen =
    Cases.liabilityCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val liabilityCaseWithStatusSuspended =
    Cases.liabilityCaseExample.copy(reference = "reference", status = CaseStatus.SUSPENDED)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(c: Case) =
    new ReopenCaseController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      mcc,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new ReopenCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      mcc,
      realAppConfig
    )

  "ReopenCaseControllerSpec" should {

    "return 303 and redirect to applicant details (case_details page) for BTI when case is referred" in {
      when(casesService.reopenCase(refEq(btiCaseWithStatusREFERRED), any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(btiCaseWithStatusOPEN))

      val result: Result = await(
        controller(btiCaseWithStatusREFERRED)
          .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(controllers.routes.CaseController.get("reference").url)
    }

    "return 303 and redirect to applicant details (case_details page) for BTI when case is suspended" in {
      when(casesService.reopenCase(refEq(btiCaseWithStatusSUSPENDED), any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(btiCaseWithStatusOPEN))

      val result: Result = await(
        controller(btiCaseWithStatusSUSPENDED)
          .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(controllers.routes.CaseController.get("reference").url)
    }

    "return 303 and redirect to case details for liability when case is suspended" in {
      when(casesService.reopenCase(refEq(liabilityCaseWithStatusSuspended), any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(liabilityCaseWithStatusOpen))

      val result: Result = await(
        controller(liabilityCaseWithStatusSuspended)
          .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(controllers.routes.CaseController.get("reference").url)
    }

    "return 303 when user has right permissions" in {
      when(casesService.reopenCase(any[Case], any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(btiCaseWithStatusOPEN))

      val result: Result = await(
        controller(btiCaseWithStatusREFERRED, Set(Permission.REOPEN_CASE))
          .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(controllers.routes.CaseController.get("reference").url)
    }

    "redirect to unauthorised when user does not have the right permissions" in {
      val result: Result = await(
        controller(btiCaseWithStatusREFERRED, Set.empty)
          .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }
}
