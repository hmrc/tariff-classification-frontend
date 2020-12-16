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

import controllers.routes._
import models._
import models.forms.CaseStatusRadioInputFormProvider
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService}
import models.forms.CorrespondenceForm
import utils.Cases
import play.api.mvc.request.RequestTarget
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.data.Forms._
import models.forms.mappings.FormMappings.fieldNonEmpty
import scala.concurrent.ExecutionContext.Implicits.global

class CreateCorrespondenceControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  val form : Form[CorrespondenceApplication]= CorrespondenceForm.newCorrespondenceForm

  private val casesService = mock[CasesService]
  private val queuesService = mock[QueuesService]
  private val operator     = mock[Operator]
  private val releaseCaseView = injector.instanceOf[views.html.release_case]
  private val releaseCaseQuestionView = injector.instanceOf[views.html.v2.release_option_choice]
  private val confirmation_case_creation = injector.instanceOf[views.html.v2.confirmation_case_creation]

  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)

  private def controller(c: Case) =
    new CreateCorrespondenceController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      queuesService,
      mcc,
      releaseCaseView,
      releaseCaseQuestionView,
      confirmation_case_creation,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new CreateCorrespondenceController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      queuesService,
      mcc,
      releaseCaseView,
      releaseCaseQuestionView,
      confirmation_case_creation,
      realAppConfig
    )


  "CreateCorrespondenceController" should {

    "return OK with correct HTML" in {
      val result = await(controller(caseWithStatusOPEN).get()(newFakeGETRequestWithCSRF(app)))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return OK when the user has the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .get()(newFakeGETRequestWithCSRF(app))
      )

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("page.title.create_correspondence.h1"))
    }

    "return unauthorised when user does not have the necessary permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.VIEW_ASSIGNED_CASES))
          .get()(newFakeGETRequestWithCSRF(app))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "redirect to Do you want to release case page POST" in {
      val popForm = form.fillAndValidate(CorrespondenceApplication())
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .post()(
            newFakePOSTRequestWithCSRF(app)
              .withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Complete.toString)
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe CompleteCaseController.completeCase("reference").url
    }
//
//    "redirect to Refer case page POST" in {
//      val result = await(
//        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
//          .onSubmit("reference")(
//            newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Refer.toString)
//          )
//      )
//
//      status(result)               shouldBe Status.SEE_OTHER
//      redirectLocation(result).get shouldBe ReferCaseController.getReferCase("reference").url
//    }
//
//    "redirect to Reject case page POST" in {
//      val result = await(
//        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
//          .onSubmit("reference")(
//            newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Reject.toString)
//          )
//      )
//
//      status(result)               shouldBe Status.SEE_OTHER
//      redirectLocation(result).get shouldBe RejectCaseController.getRejectCase("reference").url
//    }
//
//    "redirect to Suspend case page with POST" in {
//      val result = await(
//        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
//          .onSubmit("reference")(
//            newFakePOSTRequestWithCSRF(app)
//              .withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Suspend.toString)
//          )
//      )
//
//      status(result)               shouldBe Status.SEE_OTHER
//      redirectLocation(result).get shouldBe SuspendCaseController.getSuspendCase("reference").url
//    }
//
//    "redirect to Move back to queue page with POST" in {
//
//      val result = await(
//        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
//          .onSubmit("reference")(
//            newFakePOSTRequestWithCSRF(app)
//              .withTarget(RequestTarget("origin", "", Map.empty))
//              .withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.MoveBackToQueue.toString)
//          )
//      )
//
//      status(result)               shouldBe Status.SEE_OTHER
//      redirectLocation(result).get shouldBe ReassignCaseController.reassignCase("reference", "origin").url
//    }
//
//    "redirect to change case status page when form has errors" in {
//      val result =
//        controller(caseWithStatusOPEN, Set(Permission.EDIT_RULING))
//          .onSubmit("reference")(newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("caseStatus" -> ""))
//
//      contentAsString(result) should include("Select a case status")
//    }
  }
}
