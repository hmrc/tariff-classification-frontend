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

package controllers.v2

import java.time.Clock

import audit.AuditService
import com.google.inject.Provider
import connector.BindingTariffClassificationConnector
import controllers.{ControllerCommons, RequestActions, RequestActionsWithPermissions, SuccessfulRequestActions}
import javax.inject.Inject
import models._
import models.forms.{ActivityForm, ActivityFormData}
import models.viewmodels.LiabilityViewModel
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import utils.{Cases, Events}
import views.html.v2.{case_heading, liability_view}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{any, refEq, eq => meq}
import org.mockito.BDDMockito.given
import play.api.data.Form
import play.api.http.Status
import play.twirl.api.Html
import service.{EventsService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class RequestActionsWithPermissionsProvider @Inject()(implicit parse: BodyParsers.Default) extends Provider[RequestActionsWithPermissions] {

  override def get(): RequestActionsWithPermissions = {
    new RequestActionsWithPermissions(parse, Set(Permission.ADD_NOTE), c = Cases.liabilityCaseExample)
  }
}

class LiabilityControllerSpec extends UnitSpec with Matchers with BeforeAndAfterEach with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {
  private val activityForm: Form[ActivityFormData] = ActivityForm.form
  private val pagedEvent = Paged(Seq(Events.event), 1, 1, 1)
  private val queues = Seq(Queue("", "", ""))
  private val eventService = mock[EventsService]
  private val queueService = mock[QueuesService]
  private val operator = Operator(id = "id")
  private val event = mock[Event]


  override lazy val app: Application = new GuiceApplicationBuilder().overrides(
    bind[RequestActions].toProvider[RequestActionsWithPermissionsProvider],
    bind[liability_view].toInstance(mock[liability_view]),
    bind[EventsService].toInstance(mock[EventsService]),
    bind[QueuesService].toInstance(mock[QueuesService]),
    bind[AuditService].toInstance(mock[AuditService]),
    bind[BindingTariffClassificationConnector].toInstance(mock[BindingTariffClassificationConnector])
  ).build()

  override def beforeEach(): Unit = reset(inject[liability_view])

  "Calling /manage-tariff-classifications/cases/v2/:reference/liability " should {

    "return a 200 status" in {
      val expected = LiabilityViewModel.fromCase(Cases.liabilityCaseExample, Cases.operatorWithoutPermissions, pagedEvent, queues)

      when(inject[liability_view].apply(meq(expected), meq(activityForm))(any(), any(), any())) thenReturn Html("body")

      when(inject[EventsService].getFilteredEvents(any(), any(), any())(any())) thenReturn Future(pagedEvent)

      when(inject[QueuesService].getAll) thenReturn Future(queues)

      val result: Future[Result] = route(app, FakeRequest("GET", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody()).get

      status(result) shouldBe OK

      verify(inject[liability_view], times(1)).apply(meq(expected), meq(activityForm))(any(), any(), any())
    }
  }

  "Activity: Add Note" should {
    val aCase = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions))

    "add a new note when a case note is provided" in {
      val aNote = "This is a note"

      when(inject[AuditService].auditNote(any(), any(), any())(any()))

      when(inject[BindingTariffClassificationConnector].createEvent(any(), any())(any())) thenReturn Future(event)

      when(inject[EventsService].addNote(meq(aCase), meq(aNote), meq(Cases.operatorWithPermissions), any[Clock])(any[HeaderCarrier])) thenReturn Future(event)

      when(inject[EventsService].getFilteredEvents(any(), any(), any())(any())) thenReturn Future(pagedEvent)

      when(inject[QueuesService].getAll) thenReturn Future(queues)


      val result: Future[Result] =
        route(app, FakeRequest("POST", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody("note" -> aNote)).get

      status(result) shouldBe SEE_OTHER

      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/v2/123456/liability")
    }

/*    "displays an error when no case note is provided" in {
      val aValidForm = newFakePOSTRequestWithCSRF(fakeApplication, Map())
      given(eventService.getEvents(refEq(aCase.reference), refEq(NoPagination()))(any[HeaderCarrier])) willReturn successful(Paged.empty[Event])
      given(queueService.getAll) willReturn successful(Seq.empty)

      val result = controller(aCase, Set(Permission.ADD_NOTE)).addNote(aCase.reference)(aValidForm)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("error-summary")
      contentAsString(result) should include("Enter a case note")
    }

    "return OK when user has right permissions" in {
      val aCase  = Cases.btiCaseExample
      given(eventService.getEvents(refEq(aCase.reference), refEq(NoPagination()))(any[HeaderCarrier])) willReturn successful(Paged(Events.events))
      given(queueService.getAll) willReturn successful(Seq.empty)

      val result = controller(aCase,Set(Permission.ADD_NOTE)).addNote(aCase.reference)(newFakeGETRequestWithCSRF(fakeApplication))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val aCase  = Cases.btiCaseExample

      val result = controller(aCase, Set.empty).addNote(aCase.reference)(newFakeGETRequestWithCSRF(fakeApplication))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include ("unauthorized")
    }*/
  }

}
