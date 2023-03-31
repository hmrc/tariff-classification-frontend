/*
 * Copyright 2023 HM Revenue & Customs
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

import models._
import models.forms.v2.MiscellaneousForm
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.v2.create_misc
import views.html.{case_not_found, resource_not_found}

import scala.concurrent.Future
import scala.concurrent.Future.successful

class CreateMiscellenaousControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  val form: Form[MiscApplication] = MiscellaneousForm.newMiscForm

  private val casesService               = mock[CasesService]
  private val queuesService              = mock[QueuesService]
  private val releaseCaseView            = injector.instanceOf[views.html.release_case]
  private val confirmation_case_creation = injector.instanceOf[views.html.v2.confirmation_case_creation]
  private val misc_details_edit          = injector.instanceOf[views.html.v2.misc_details_edit]
  private val createMisc                 = injector.instanceOf[create_misc]
  private val caseNotFound               = injector.instanceOf[case_not_found]
  private val resourceNotFound           = injector.instanceOf[resource_not_found]

  private val caseWithStatusOPEN =
    Cases.miscellaneousCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new CreateMiscellaneousController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      queuesService,
      mcc,
      releaseCaseView,
      confirmation_case_creation,
      misc_details_edit,
      createMisc,
      caseNotFound,
      resourceNotFound,
      realAppConfig
    )

  "CreateMiscellaneousController" should {

    "return OK with correct HTML" in {
      val result =
        await(controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES)).get()(newFakeGETRequestWithCSRF()))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return OK when the user has the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .get()(newFakeGETRequestWithCSRF())
      )

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("page.title.create_misc.h1"))
    }

    "return unauthorised when user does not have the necessary permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.VIEW_ASSIGNED_CASES))
          .get()(newFakeGETRequestWithCSRF())
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "display Misc details page if form has errors POST" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.miscellaneousCaseExample))
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .post()(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody(
                "name"        -> "",
                "contactName" -> "",
                "caseType"    -> "Other"
              )
          )
      )

      contentAsString(result) should include(messages("page.title.create_misc.h1"))

    }

    "display no results found when a queue is not found GET" in {
      given(casesService.createCase(any[MiscApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.miscellaneousCaseExample.copy(queueId = Some("queue"))))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(Some(Cases.miscellaneousCaseExample.copy(queueId = Some("queue")))))

      given(queuesService.getOneById(any[String]))
        .willReturn(successful(None))

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .displayConfirmation("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)          shouldBe Status.OK
      contentAsString(result) should include("Case Queue not found.")
    }

    "display no results found when a case is not found GET" in {
      given(casesService.createCase(any[MiscApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.miscellaneousCaseExample.copy(queueId = Some("queue"))))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(None))

      given(queuesService.getOneById(any[String]))
        .willReturn(successful(None))

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .displayConfirmation("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)          shouldBe Status.OK
      contentAsString(result) should include("We could not find a Case with reference: reference")
    }

    "editMiscDetails" should {

      "return 200" in {
        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_MISCELLANEOUS))
            .editMiscDetails("reference")(newFakePOSTRequestWithCSRF())
        )
        status(result) shouldBe OK
      }

      "return unauthorised if the user does not have the right permissions" in {

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.VIEW_ASSIGNED_CASES))
            .editMiscDetails("reference")(newFakePOSTRequestWithCSRF())
        )
        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get should include("unauthorized")
      }
    }

    "postMiscDetails" should {

      "redirect back to controller if the form has been submitted successfully" in {

        when(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])) thenReturn Future(
          Cases.aCorrespondenceCase()
        )

        val fakeReq = newFakePOSTRequestWithCSRF(
          Map(
            "summary"             -> "A short summary",
            "detailedDescription" -> "A detailed desc",
            "caseType"            -> "Appeals",
            "contactName"         -> "Name"
          )
        )

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_MISCELLANEOUS))
            .postMiscDetails("reference")(fakeReq)
        )

        status(result) shouldBe SEE_OTHER

        locationOf(result) shouldBe Some(
          "/manage-tariff-classifications/cases/v2/" + "reference" + "/miscellaneous"
        )
      }

      "return back to the view if form fails to validate" in {
        when(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])) thenReturn Future(
          Cases.aCaseWithCompleteDecision
        )

        val fakeReq = newFakePOSTRequestWithCSRF(
          Map(
            "summary"             -> "",
            "detailedDescription" -> "A detailed desc",
            "caseType"            -> "Appeals"
          )
        )

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_MISCELLANEOUS))
            .postMiscDetails("reference")(fakeReq)
        )

        status(result) shouldBe OK
      }
    }
  }
}
