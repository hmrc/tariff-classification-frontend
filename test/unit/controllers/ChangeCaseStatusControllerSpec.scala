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

import controllers.routes._
import models._
import models.forms.CaseStatusRadioInputFormProvider
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.CasesService
import utils.Cases
import play.api.mvc.request.RequestTarget
import views.html.change_case_status

import scala.concurrent.ExecutionContext.Implicits.global

class ChangeCaseStatusControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator(id = "id")
  private val changeCaseStatus = injector.instanceOf[change_case_status]
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)

  private def controller(c: Case) =
    new ChangeCaseStatusController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      mcc,
      changeCaseStatus,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new ChangeCaseStatusController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      mcc,
      changeCaseStatus,
      realAppConfig
    )

  val form = new CaseStatusRadioInputFormProvider()()

  "ChangeCaseStatusControllerSpec" should {

    "return OK with correct HTML" in {
      val result = await(controller(caseWithStatusOPEN).onPageLoad("reference")(newFakeGETRequestWithCSRF()))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return OK when the user has the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
          .onPageLoad("reference")(newFakeGETRequestWithCSRF())
      )

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("change_case_status_legend", "Laptop"))
    }

    "return unauthorised when user does not have the necessary permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.VIEW_ASSIGNED_CASES))
          .onPageLoad("reference")(newFakeGETRequestWithCSRF())
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "redirect to Complete case page POST" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
          .onSubmit("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Complete.toString)
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe CompleteCaseController.completeCase("reference").url
    }

    "redirect to Refer case page POST" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
          .onSubmit("reference")(
            newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Refer.toString)
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe ReferCaseController.getReferCaseReason("reference").url
    }

    "redirect to Reject case page POST" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
          .onSubmit("reference")(
            newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Reject.toString)
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe RejectCaseController.getRejectCaseReason("reference").url
    }

    "redirect to Suspend case page with POST" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
          .onSubmit("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Suspend.toString)
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe SuspendCaseController.getSuspendCaseReason("reference").url
    }

    "redirect to Move back to queue page with POST" in {

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
          .onSubmit("reference")(
            newFakePOSTRequestWithCSRF()
              .withTarget(RequestTarget("origin", "", Map.empty))
              .withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.MoveBackToQueue.toString)
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe ReassignCaseController.reassignCase("reference", "origin").url
    }

    "redirect to change case status page when form has errors" in {
      val result =
        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
          .onSubmit("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("caseStatus" -> ""))

      contentAsString(result) should include("Select a case status")
    }
  }
}
