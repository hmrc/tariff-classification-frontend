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

import controllers.routes._
import models._
import models.forms.CorrespondenceForm
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.v2.create_correspondence
import views.html.{case_not_found, resource_not_found}

import scala.concurrent.Future
import scala.concurrent.Future.successful

class CreateCorrespondenceControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  val form: Form[CorrespondenceApplication] = CorrespondenceForm.newCorrespondenceForm

  private val casesService                = mock[CasesService]
  private val queuesService               = mock[QueuesService]
  private val operator                    = Operator("id")
  private val releaseCaseView             = injector.instanceOf[views.html.release_case]
  private val releaseCaseQuestionView     = injector.instanceOf[views.html.v2.release_option_choice]
  private val confirmation_case_creation  = injector.instanceOf[views.html.v2.confirmation_case_creation]
  private val correspondence_details_edit = injector.instanceOf[views.html.v2.correspondence_details_edit]
  private val correspondence_contact_edit = injector.instanceOf[views.html.v2.correspondence_contact_edit]
  private val createCorrespondence        = injector.instanceOf[create_correspondence]
  private val caseNotFound                = injector.instanceOf[case_not_found]
  private val resourceNotFound            = injector.instanceOf[resource_not_found]

  private val caseWithStatusOPEN =
    Cases.correspondenceCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)

  private def controller(c: Case) =
    new CreateCorrespondenceController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      queuesService,
      mcc,
      releaseCaseView,
      releaseCaseQuestionView,
      confirmation_case_creation,
      correspondence_details_edit,
      correspondence_contact_edit,
      createCorrespondence,
      caseNotFound,
      resourceNotFound,
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
      correspondence_details_edit,
      correspondence_contact_edit,
      createCorrespondence,
      caseNotFound,
      resourceNotFound,
      realAppConfig
    )

  "CreateCorrespondenceController" should {

    "return OK with correct HTML" in {
      val result = await(controller(caseWithStatusOPEN).get()(newFakeGETRequestWithCSRF()))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return OK when the user has the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .get()(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentType(result)   shouldBe Some("text/html")
      charset(result)       shouldBe Some("utf-8")
      contentAsString(result) should include(messages("page.title.create_correspondence.h1"))
    }

    "return unauthorised when user does not have the necessary permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.VIEW_ASSIGNED_CASES))
          .get()(newFakeGETRequestWithCSRF())
      )

      status(result)             shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "redirect to Do you want to release case page POST" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.correspondenceCaseExample))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(Some(Cases.correspondenceCaseExample)))
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .post()(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody(
                "summary"      -> "dummy1",
                "source"       -> "dummy2",
                "contactEmail" -> "dummy@email.com"
              )
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe CreateCorrespondenceController.displayQuestion("1").url
    }

    "display Corr details page if form has errors POST" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.correspondenceCaseExample))
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .post()(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody(
                "summary"      -> "",
                "source"       -> "",
                "contactEmail" -> "dummyemailcom"
              )
          )
      )

      contentAsString(result) should include(messages("page.title.create_correspondence.h1"))

    }

    "Release choice should redirect to Release Case if Yes POST" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES, Permission.RELEASE_CASE))
          .postChoice("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody(
                "choice" -> "Yes"
              )
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe ReleaseCaseController.releaseCase("reference").url
    }

    "Release choice should redirect to Confirmation page if No POST" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES, Permission.RELEASE_CASE))
          .postChoice("reference")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody(
                "choice" -> "No"
              )
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe CreateCorrespondenceController.displayConfirmation("reference").url
    }

    "Release choice should display Do you want to release case page if form has errors POST" in {
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(Some(Cases.correspondenceCaseExample)))
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES, Permission.RELEASE_CASE))
          .postChoice("reference")(
            newFakePOSTRequestWithCSRF()
          )
      )
      status(result) shouldBe Status.OK

      contentAsString(result) should include(messages("release_case.question.error"))

    }

    "Release choice should display case not found when case not found and form has errors POST" in {
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(None))
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES, Permission.RELEASE_CASE))
          .postChoice("reference")(
            newFakePOSTRequestWithCSRF()
          )
      )
      status(result) shouldBe Status.OK

      contentAsString(result) should include(messages("We could not find a Case with reference: reference"))

    }

    "display Do you want to release case page GET" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.correspondenceCaseExample))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(Some(Cases.correspondenceCaseExample)))
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES, Permission.RELEASE_CASE))
          .displayQuestion("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result) shouldBe Status.OK

    }

    "display Confirmation case page for creating a correspondence with no queue GET" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.correspondenceCaseExample.copy(queueId = None)))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(Some(Cases.correspondenceCaseExample.copy(queueId = None))))
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .displayConfirmation("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentAsString(result) should include("case has been created")
    }

    "display Confirmation case page for creating a correspondence with a queue GET" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.correspondenceCaseExample.copy(queueId = Some("queue"))))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(Some(Cases.correspondenceCaseExample.copy(queueId = Some("queue")))))

      given(queuesService.getOneById(any[String]))
        .willReturn(successful(Some(Queue("queue", "queue", "queue"))))

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .displayConfirmation("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentAsString(result) should include("case has been released to a team")
    }

    "display no results found when a queue is not found GET" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.correspondenceCaseExample.copy(queueId = Some("queue"))))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(Some(Cases.correspondenceCaseExample.copy(queueId = Some("queue")))))

      given(queuesService.getOneById(any[String]))
        .willReturn(successful(None))

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .displayConfirmation("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentAsString(result) should include("Case Queue not found.")
    }

    "display no results found when a case is not found GET" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.correspondenceCaseExample.copy(queueId = Some("queue"))))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(None))

      given(queuesService.getOneById(any[String]))
        .willReturn(successful(None))

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .displayConfirmation("reference")(newFakePOSTRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentAsString(result) should include("We could not find a Case with reference: reference")
    }

    "editCorrespondence" should {

      "return 200 and load the editLiability form" in {
        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_CORRESPONDENCE))
            .editCorrespondence("reference")(newFakePOSTRequestWithCSRF())
        )
        status(result) shouldBe OK
      }

      "return unauthorised if the user does not have the right permissions" in {

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.VIEW_ASSIGNED_CASES))
            .editCorrespondence("reference")(newFakePOSTRequestWithCSRF())
        )
        status(result)             shouldBe SEE_OTHER
        redirectLocation(result).get should include("unauthorized")
      }
    }

    "postCorrespondenceDetails" should {

      "redirect back to controller if the form has been submitted successfully" in {

        when(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])) thenReturn Future(
          Cases.aCorrespondenceCase()
        )

        val fakeReq = newFakePOSTRequestWithCSRF(
          Map(
            "summary"             -> "A short summary",
            "detailedDescription" -> "A detailed desc"
          )
        )

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_CORRESPONDENCE))
            .postCorrespondenceDetails("reference")(fakeReq)
        )

        status(result) shouldBe SEE_OTHER

        locationOf(result) shouldBe Some(
          "/manage-tariff-classifications/cases/v2/" + "reference" + "/correspondence"
        )
      }

      "return back to the view if form fails to validate" in {
        when(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])) thenReturn Future(
          Cases.aCaseWithCompleteDecision
        )

        val fakeReq = newFakePOSTRequestWithCSRF(
          Map(
            "summary"             -> "",
            "detailedDescription" -> "A detailed desc"
          )
        )

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_CORRESPONDENCE))
            .postCorrespondenceDetails("reference")(fakeReq)
        )

        status(result) shouldBe OK
      }
    }

    "editCorrespondenceContact" should {

      "return 200 and load the editLiability form" in {
        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_CORRESPONDENCE))
            .editCorrespondenceContact("reference")(newFakePOSTRequestWithCSRF())
        )
        status(result) shouldBe OK
      }

      "return unauthorised if the user does not have the right permissions" in {

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.VIEW_ASSIGNED_CASES))
            .editCorrespondenceContact("reference")(newFakePOSTRequestWithCSRF())
        )
        status(result)             shouldBe SEE_OTHER
        redirectLocation(result).get should include("unauthorized")
      }
    }

    "postCorrespondenceContact" should {

      "redirect back to controller if the form has been submitted successfully" in {

        when(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])) thenReturn Future(
          Cases.aCorrespondenceCase()
        )

        val fakeReq = newFakePOSTRequestWithCSRF(
          Map(
            "correspondenceStarter" -> "Starter",
            "name"                  -> "a name",
            "email"                 -> "anemail@some.com",
            "buildingAndStreet"     -> "New building",
            "townOrCity"            -> "Old Town",
            "agentName"             -> "Agent 007"
          )
        )

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_CORRESPONDENCE))
            .postCorrespondenceContact("reference")(fakeReq)
        )

        status(result) shouldBe SEE_OTHER

        locationOf(result) shouldBe Some(
          "/manage-tariff-classifications/cases/v2/" + "reference" + "/correspondence#contact_details_tab"
        )
      }

      "return back to the view if form fails to validate" in {
        when(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])) thenReturn Future(
          Cases.aCaseWithCompleteDecision
        )

        val fakeReq = newFakePOSTRequestWithCSRF(
          Map(
            "correspondenceStarter" -> "",
            "name"                  -> "a name",
            "email"                 -> "anemail@some.com",
            "buildingAndStreet"     -> "New building",
            "townOrCity"            -> "Old Town",
            "agentName"             -> "Agent 007"
          )
        )

        val result = await(
          controller(caseWithStatusOPEN, Set(Permission.EDIT_CORRESPONDENCE))
            .postCorrespondenceContact("reference")(fakeReq)
        )

        status(result) shouldBe OK
      }
    }

    def asFormParams(cc: Product): List[(String, String)] =
      cc.getClass.getDeclaredFields.toList
        .map { f =>
          f.setAccessible(true)
          (f.getName, f.get(cc))
        }
        .filterNot(_._1 == "serialVersionUID")
        .filterNot(_._1 == "MODULE$")
        .flatMap {
          case (n, l: List[_]) if l.headOption.exists(_.isInstanceOf[Product]) =>
            l.zipWithIndex.flatMap { case (x, i) =>
              asFormParams(x.asInstanceOf[Product]).map { case (k, v) => (s"$n[$i].$k", v) }
            }
          case (n, Some(p: Product)) => asFormParams(p).map { case (k, v) => (s"$n.$k", v) }
          case (n, Some(a))          => List((n, a.toString))
          case (n, None)             => List((n, ""))
          case (n, p: Product)       => asFormParams(p).map { case (k, v) => (s"$n.$k", v) }
          case (n, a)                => List((n, a.toString))
        }
  }
}
