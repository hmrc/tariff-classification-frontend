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

import connector.BindingTariffClassificationConnector
import controllers.v2.{AtarController, CorrespondenceController, LiabilityController, MiscellaneousController}
import models._
import models.forms.{ActivityFormData, MessageFormData}
import models.request.AuthenticatedCaseRequest
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.Html
import service._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.Cases._

import java.time.{Clock, Instant}
import scala.concurrent.Future

class CaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val keywordsService          = mock[KeywordsService]
  private val eventService             = mock[EventsService]
  private val operator                 = Operator(id = "id")
  private val event                    = mock[Event]
  private val atarController           = mock[AtarController]
  private val liabilityController      = mock[LiabilityController]
  private val casesService             = mock[CasesService]
  private val correspondenceController = mock[CorrespondenceController]
  private val miscellaneousController  = mock[MiscellaneousController]
  private val connector                = mock[BindingTariffClassificationConnector]

  override protected def beforeEach(): Unit =
    reset(
      keywordsService,
      eventService,
      casesService,
      event,
      atarController,
      liabilityController,
      correspondenceController,
      miscellaneousController,
      connector
    )

  private def controller(c: Case) = new CaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = c),
    keywordsService,
    eventService,
    casesService,
    mcc,
    liabilityController,
    atarController,
    correspondenceController,
    miscellaneousController,
    redirectService,
    realAppConfig
  )

  private def controller(c: Case, permission: Set[Permission]) = new CaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = c),
    keywordsService,
    eventService,
    casesService,
    mcc,
    liabilityController,
    atarController,
    correspondenceController,
    miscellaneousController,
    redirectService,
    realAppConfig
  )

  "Case get" should {
    "redirect to correct page" when {
      "case is an ATaR" in {
        val c      = aCase(withReference("reference"), withBTIApplication)
        val result = await(controller(c).get("reference")(fakeRequest))

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(v2.routes.AtarController.displayAtar("reference").url)
      }

      "case is a Liability" in {
        val c      = aCase(withReference("reference"), withLiabilityApplication())
        val result = controller(c).get("reference")(fakeRequest)

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(v2.routes.LiabilityController.displayLiability("reference").url)
      }

      "case is a Correspondence" in {
        val c      = aCase(withReference("reference"), withCorrespondenceApplication)
        val result = controller(c).get("reference")(fakeRequest)

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(v2.routes.CorrespondenceController.displayCorrespondence("reference").url)
      }

      "case is a Miscellaneous" in {
        val c      = aCase(withReference("reference"), withMiscellaneousApplication)
        val result = controller(c).get("reference")(fakeRequest)

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(v2.routes.MiscellaneousController.displayMiscellaneous("reference").url)
      }
    }
  }

  "Case sampleDetails" should {
    "redirect to correct page" when {
      "case is an ATaR" in {
        val c      = aCase(withReference("reference"), withBTIApplication)
        val result = await(controller(c).sampleDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.AtarController.displayAtar("reference").withFragment(Tab.SAMPLE_TAB.name).path
        )
      }

      "case is a Liability" in {
        val c      = aCase(withReference("reference"), withLiabilityApplication())
        val result = await(controller(c).sampleDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.LiabilityController.displayLiability("reference").withFragment(Tab.SAMPLE_TAB.name).path
        )
      }

      "case is a Correspondence" in {
        val c      = aCase(withReference("reference"), withCorrespondenceApplication)
        val result = await(controller(c).sampleDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.CorrespondenceController
            .displayCorrespondence("reference")
            .withFragment(Tab.SAMPLE_TAB.name)
            .path
        )
      }

      "case is a Miscellaneous" in {
        val c      = aCase(withReference("reference"), withMiscellaneousApplication)
        val result = await(controller(c).sampleDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.MiscellaneousController
            .displayMiscellaneous("reference")
            .withFragment(Tab.SAMPLE_TAB.name)
            .path
        )
      }
    }
  }

  "Case rulingDetails" should {
    "redirect to correct page" when {
      "case is an ATaR" in {
        val c      = aCase(withReference("reference"), withBTIApplication)
        val result = await(controller(c).rulingDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.AtarController.displayAtar("reference").withFragment(Tab.RULING_TAB.name).path
        )
      }

      "case is a Liability" in {
        val c      = aCase(withReference("reference"), withLiabilityApplication())
        val result = await(controller(c).rulingDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.LiabilityController.displayLiability("reference").withFragment(Tab.RULING_TAB.name).path
        )
      }

      "case is a Miscellaneous" in {
        val c      = aCase(withReference("reference"), withMiscellaneousApplication)
        val result = await(controller(c).rulingDetails("reference")(fakeRequest))

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(v2.routes.MiscellaneousController.displayMiscellaneous("reference").path)
      }

      "case is a Correspondence" in {
        val c      = aCase(withReference("reference"), withCorrespondenceApplication)
        val result = await(controller(c).rulingDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.CorrespondenceController.displayCorrespondence("reference").path
        )
      }
    }
  }

  "Case activityDetails" should {
    "redirect to correct page" when {
      "case is an ATaR" in {
        val c      = aCase(withReference("reference"), withBTIApplication)
        val result = await(controller(c).activityDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.AtarController.displayAtar("reference").withFragment(Tab.ACTIVITY_TAB.name).path
        )
      }

      "case is a Liability" in {
        val c      = aCase(withReference("reference"), withLiabilityApplication())
        val result = await(controller(c).activityDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.LiabilityController.displayLiability("reference").withFragment(Tab.ACTIVITY_TAB.name).path
        )
      }

      "case is an Correspondence" in {
        val c      = aCase(withReference("reference"), withCorrespondenceApplication)
        val result = await(controller(c).activityDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.CorrespondenceController.displayCorrespondence("reference").withFragment(Tab.ACTIVITY_TAB.name).path
        )
      }

      "case is an Miscellaneous" in {
        val c      = aCase(withReference("reference"), withMiscellaneousApplication)
        val result = await(controller(c).activityDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.MiscellaneousController.displayMiscellaneous("reference").withFragment(Tab.ACTIVITY_TAB.name).path
        )
      }

    }
  }

  "Case keywordsDetails" should {
    "redirect to correct page" when {
      "case is an ATaR" in {
        val c      = aCase(withReference("reference"), withBTIApplication)
        val result = await(controller(c).keywordsDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.AtarController.displayAtar("reference").withFragment(Tab.KEYWORDS_TAB.name).path
        )
      }

      "case is a Liability" in {
        val c      = aCase(withReference("reference"), withLiabilityApplication())
        val result = await(controller(c).keywordsDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.LiabilityController.displayLiability("reference").withFragment(Tab.KEYWORDS_TAB.name).path
        )
      }
    }
  }

  "Case attachmentsDetails" should {
    "redirect to correct page" when {
      "case is an ATaR" in {
        val c      = aCase(withReference("reference"), withBTIApplication)
        val result = await(controller(c).attachmentsDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.AtarController.displayAtar("reference").withFragment(Tab.ATTACHMENTS_TAB.name).path
        )
      }

      "case is a Liability" in {
        val c      = aCase(withReference("reference"), withLiabilityApplication())
        val result = await(controller(c).attachmentsDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.LiabilityController.displayLiability("reference").withFragment(Tab.ATTACHMENTS_TAB.name).path
        )
      }

      "case is a Correspondence" in {
        val c      = aCase(withReference("reference"), withCorrespondenceApplication)
        val result = await(controller(c).attachmentsDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.CorrespondenceController
            .displayCorrespondence("reference")
            .withFragment(Tab.ATTACHMENTS_TAB.name)
            .path
        )
      }

      "case is a Miscellaneous" in {
        val c      = aCase(withReference("reference"), withMiscellaneousApplication)
        val result = await(controller(c).attachmentsDetails("reference")(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(
          v2.routes.MiscellaneousController
            .displayMiscellaneous("reference")
            .withFragment(Tab.ATTACHMENTS_TAB.name)
            .path
        )
      }

    }
  }

  "Case addNote" should {
    val aLiabilityCase = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions))
    val anAtarCase     = Cases.btiCaseExample.copy(assignee       = Some(Cases.operatorWithAddAttachment))

    "add a new note when a case note is provided" in {
      val aNote = "This is a note"

      when(
        eventService.addNote(refEq(aLiabilityCase), refEq(aNote), any[Operator], any[Clock])(
          any[HeaderCarrier]
        )
      ) thenReturn Future(event)

      val fakeReq = newFakePOSTRequestWithCSRF(Map("note" -> aNote))
      val result: Future[Result] =
        controller(aLiabilityCase, Set(Permission.ADD_NOTE)).addNote(aLiabilityCase.reference)(fakeReq)

      status(result)     shouldBe SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.activityDetails(aLiabilityCase.reference).path)
    }

    "not add a new note when a case note is not provided (liability)" in {
      val aNote   = ""
      val fakeReq = newFakePOSTRequestWithCSRF(Map("note" -> aNote))

      when(
        liabilityController
          .renderView(any[Option[String]], any[Form[ActivityFormData]], any[Form[String]], any[Form[String]])(
            any[AuthenticatedCaseRequest[_]]
          )
      ) thenReturn Future.successful(Html("error"))

      val result: Future[Result] =
        controller(aLiabilityCase, Set(Permission.ADD_NOTE)).addNote(aLiabilityCase.reference)(fakeReq)

      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) should include("error")

      verifyNoMoreInteractions(eventService)
    }

    "not add a new note when a case note is not provided (atar)" in {
      val aNote   = ""
      val fakeReq = newFakePOSTRequestWithCSRF(Map("note" -> aNote))

      when(
        atarController
          .renderView(any[Option[String]], any[Form[ActivityFormData]], any[Form[String]], any[Form[String]])(
            any[AuthenticatedCaseRequest[_]]
          )
      ) thenReturn Future.successful(Html("error"))

      val result: Future[Result] =
        controller(anAtarCase, Set(Permission.ADD_NOTE)).addNote(anAtarCase.reference)(fakeReq)

      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) should include("error")

      verifyNoMoreInteractions(eventService)
    }

    "redirect to unauthorised if the user does not have the right permissions" in {
      val aNote                  = "This is a note"
      val fakeReq                = newFakePOSTRequestWithCSRF(Map("note" -> aNote))
      val result: Future[Result] = controller(aLiabilityCase, Set()).addNote(aLiabilityCase.reference)(fakeReq)
      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Case addKeyword" should {
    val aCase = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithKeywordsPermissions))

    "redirect back to display case if form submitted successfully" in {
      val keyword = "pajamas"

      when(keywordsService.addKeyword(refEq(aCase), refEq(keyword), any[Operator])(any[HeaderCarrier])) thenReturn Future(
        aCase
      )

      val fakeReq = newFakePOSTRequestWithCSRF(Map("keyword" -> keyword))
      val result: Future[Result] =
        controller(aCase, Set(Permission.KEYWORDS)).addKeyword(aCase.reference)(fakeReq)

      status(result)     shouldBe SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.keywordsDetails(aCase.reference).path)
    }

    "return to view if form fails to validate" in {
      val keyword = ""
      val fakeReq = newFakePOSTRequestWithCSRF(Map("keyword" -> keyword))

      when(
        liabilityController
          .renderView(any[Option[String]], any[Form[ActivityFormData]], any[Form[String]], any[Form[String]])(
            any[AuthenticatedCaseRequest[_]]
          )
      ) thenReturn Future.successful(Html("error"))

      val result: Future[Result] =
        controller(aCase, Set(Permission.KEYWORDS)).addKeyword(aCase.reference)(fakeReq)

      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) should include("error")

      verifyNoMoreInteractions(keywordsService)
    }

    "redirect to unauthorised if the user does not have the right permissions" in {
      val keyword                = "pajamas"
      val fakeReq                = newFakePOSTRequestWithCSRF(Map("keyword" -> keyword))
      val result: Future[Result] = controller(aCase, Set()).addKeyword(aCase.reference)(fakeReq)
      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Case removeKeyword" should {
    val aCase = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithKeywordsPermissions))

    "remove keyword and return to case view" in {
      val keyword = "llamas"
      val fakeReq = newFakeGETRequestWithCSRF()

      when(keywordsService.removeKeyword(refEq(aCase), refEq(keyword), any[Operator])(any[HeaderCarrier])) thenReturn Future(
        aCase
      )

      val result: Future[Result] =
        controller(aCase, Set(Permission.KEYWORDS)).removeKeyword(aCase.reference, keyword)(fakeReq)

      status(result)     shouldBe SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.keywordsDetails(aCase.reference).path)
    }

    "redirect to unauthorised if the user does not have the right permissions" in {
      val keyword                = "llamas"
      val fakeReq                = newFakeGETRequestWithCSRF()
      val result: Future[Result] = controller(aCase, Set()).removeKeyword(aCase.reference, keyword)(fakeReq)
      status(result)               shouldBe SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Case addMessage" should {
    val aMessage = Message(Cases.operatorWithPermissions.name.get, Instant.now(), "message to be added")

    val anExampleCorrespondenceCase =
      Cases.correspondenceCaseExample.copy(assignee = Some(Cases.operatorWithPermissions))
    val updatedCorrespondenceCase = aCorrespondenceCase().copy(
      assignee    = Some(Cases.operatorWithPermissions),
      application = correspondenceExample.copy(messagesLogged = List(aMessage))
    )
    val anExampleMiscellaneousCase = Cases.miscellaneousCaseExample.copy(assignee = Some(Cases.operatorWithPermissions))
    val updatedMiscellaneousCase = aMiscellaneousCase().copy(
      assignee    = Some(Cases.operatorWithPermissions),
      application = miscExample.copy(messagesLogged = List(aMessage))
    )

    "add a new message when a case message is provided for a correspondence case" in {

      when(
        casesService.addMessage(refEq(anExampleCorrespondenceCase), any[Message], any[Operator])(any[HeaderCarrier])
      ) thenReturn Future(updatedCorrespondenceCase)

      val fakeReq = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("message" -> aMessage.message)
      val result: Future[Result] = controller(anExampleCorrespondenceCase, Set(Permission.ADD_MESSAGE))
        .addMessage(anExampleCorrespondenceCase.reference)(fakeReq)

      status(result) shouldBe SEE_OTHER
      locationOf(result) shouldBe Some(
        routes.CaseController.get(anExampleCorrespondenceCase.reference).withFragment(Tab.MESSAGES_TAB.name).path
      )
    }

    "add a new message when a case message is provided for a miscellaneous case" in {

      when(
        casesService.addMessage(refEq(anExampleMiscellaneousCase), any[Message], any[Operator])(any[HeaderCarrier])
      ) thenReturn Future(updatedMiscellaneousCase)

      val fakeReq = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("message" -> aMessage.message)
      val result: Future[Result] = controller(anExampleMiscellaneousCase, Set(Permission.ADD_MESSAGE))
        .addMessage(anExampleMiscellaneousCase.reference)(fakeReq)

      status(result) shouldBe SEE_OTHER
      locationOf(result) shouldBe Some(
        routes.CaseController.get(anExampleMiscellaneousCase.reference).withFragment(Tab.MESSAGES_TAB.name).path
      )
    }

    "not add a new message when a case note is not provided" in {
      val aMessage = ""
      val fakeReq  = newFakePOSTRequestWithCSRF(Map("message" -> aMessage))

      when(
        correspondenceController
          .renderView(any[Option[String]], any[Form[ActivityFormData]], any[Form[MessageFormData]], any[Form[String]])(
            any[AuthenticatedCaseRequest[_]]
          )
      ) thenReturn Future.successful(Html("error"))

      val result: Future[Result] = controller(anExampleCorrespondenceCase, Set(Permission.ADD_MESSAGE))
        .addMessage(anExampleCorrespondenceCase.reference)(fakeReq)

      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) should include("error")

      verifyNoMoreInteractions(casesService)
    }

    "redirect to unauthorised if the user does not have the right permissions" in {
      val aMessages = "This is a message"
      val fakeReq   = newFakePOSTRequestWithCSRF(Map("message" -> aMessages))
      val result: Future[Result] =
        controller(anExampleCorrespondenceCase, Set()).addMessage(anExampleCorrespondenceCase.reference)(fakeReq)
      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }
}
