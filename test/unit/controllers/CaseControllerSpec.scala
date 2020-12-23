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

import java.time.Clock

import controllers.v2.{AtarController, LiabilityController}
import models.{Case, Event, Operator, Permission}
import models.forms.ActivityFormData
import models.request.AuthenticatedCaseRequest
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.mvc.{Results, Result}
import play.api.test.Helpers._
import service._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.Cases._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val keywordsService     = mock[KeywordsService]
  private val eventService        = mock[EventsService]
  private val operator            = Operator(id = "id")
  private val event               = mock[Event]
  private val atarController      = mock[AtarController]
  private val liabilityController = mock[LiabilityController]

  override protected def beforeEach(): Unit =
    reset(
      keywordsService,
      eventService,
      event,
      atarController,
      liabilityController
    )

  private def controller(c: Case) = new CaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = c),
    keywordsService,
    eventService,
    mcc,
    liabilityController,
    atarController,
    realAppConfig
  )

  private def controller(c: Case, permission: Set[Permission]) = new CaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = c),
    keywordsService,
    eventService,
    mcc,
    liabilityController,
    atarController,
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
    }
  }

  "Case addNote" should {
    val aCase = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions))

    "add a new note when a case note is provided" in {
      val aNote = "This is a note"

      when(
        eventService.addNote(refEq(aCase), refEq(aNote), any[Operator], any[Clock])(
          any[HeaderCarrier]
        )
      ) thenReturn Future(event)

      val fakeReq                = newFakePOSTRequestWithCSRF(app, Map("note" -> aNote))
      val result: Future[Result] = controller(aCase, Set(Permission.ADD_NOTE)).addNote(aCase.reference)(fakeReq)

      status(result)     shouldBe SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.activityDetails(aCase.reference).path)
    }

    "not add a new note when a case note is not provided" in {
      val aNote   = ""
      val fakeReq = newFakePOSTRequestWithCSRF(app, Map("note" -> aNote))

      when(
        liabilityController.renderView(any[Form[ActivityFormData]], any[Form[String]], any[Form[String]])(
          any[AuthenticatedCaseRequest[_]]
        )
      ) thenReturn Future.successful(Results.Ok("error"))

      val result: Future[Result] = controller(aCase, Set(Permission.ADD_NOTE)).addNote(aCase.reference)(fakeReq)

      status(result)          shouldBe OK
      contentAsString(result) should include("error")

      verifyZeroInteractions(eventService)
    }

    "redirect to unauthorised if the user does not have the right permissions" in {
      val aNote                  = "This is a note"
      val fakeReq                = newFakePOSTRequestWithCSRF(app, Map("note" -> aNote))
      val result: Future[Result] = controller(aCase, Set()).addNote(aCase.reference)(fakeReq)
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

      val fakeReq = newFakePOSTRequestWithCSRF(app, Map("keyword" -> keyword))
      val result: Future[Result] =
        controller(aCase, Set(Permission.KEYWORDS)).addKeyword(aCase.reference)(fakeReq)

      status(result)     shouldBe SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.keywordsDetails(aCase.reference).path)
    }

    "return to view if form fails to validate" in {
      val keyword = ""
      val fakeReq = newFakePOSTRequestWithCSRF(app, Map("keyword" -> keyword))

      when(
        liabilityController.renderView(any[Form[ActivityFormData]], any[Form[String]], any[Form[String]])(
          any[AuthenticatedCaseRequest[_]]
        )
      ) thenReturn Future.successful(Results.Ok("error"))

      val result: Future[Result] =
        controller(aCase, Set(Permission.KEYWORDS)).addKeyword(aCase.reference)(fakeReq)

      status(result)          shouldBe OK
      contentAsString(result) should include("error")

      verifyZeroInteractions(keywordsService)
    }

    "redirect to unauthorised if the user does not have the right permissions" in {
      val keyword                = "pajamas"
      val fakeReq                = newFakePOSTRequestWithCSRF(app, Map("keyword" -> keyword))
      val result: Future[Result] = controller(aCase, Set()).addKeyword(aCase.reference)(fakeReq)
      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Case removeKeyword" should {
    val aCase = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithKeywordsPermissions))

    "remove keyword and return to case view" in {
      val keyword = "llamas"
      val fakeReq = newFakeGETRequestWithCSRF(app)

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
      val fakeReq                = newFakeGETRequestWithCSRF(app)
      val result: Future[Result] = controller(aCase, Set()).removeKeyword(aCase.reference, keyword)(fakeReq)
      status(result)               shouldBe SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }
}
