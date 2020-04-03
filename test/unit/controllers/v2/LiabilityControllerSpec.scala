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

import java.time.{Clock, Instant}

import com.google.inject.Provider
import controllers.{ControllerCommons, RequestActions, RequestActionsWithPermissions}
import javax.inject.Inject
import models.{Case, _}
import models.forms.{ActivityForm, ActivityFormData}
import models.viewmodels.{ActivityViewModel, LiabilityViewModel}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import service.{EventsService, FileStoreService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.{Cases, Dates, Events}
import views.html.v2.{case_heading, liability_view}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RequestActionsWithPermissionsProvider @Inject()(implicit parse: BodyParsers.Default) extends Provider[RequestActionsWithPermissions] {

  override def get(): RequestActionsWithPermissions = {
    new RequestActionsWithPermissions(
      parse, Set(Permission.ADD_NOTE),
      c = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions)),
      op = Cases.operatorWithPermissions
    )
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
    bind[case_heading].toInstance(mock[case_heading]),
    bind[FileStoreService].toInstance(mock[FileStoreService])
  ).build()

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = reset(inject[liability_view])

  "Calling /manage-tariff-classifications/cases/v2/:reference/liability " should {

    "return a 200 status" in {

      when(inject[EventsService].getFilteredEvents(any(), any(), any())(any())) thenReturn Future(pagedEvent)

      when(inject[QueuesService].getAll) thenReturn Future(queues)
      val expectedLiabilityViewModel = LiabilityViewModel.fromCase(Cases.liabilityCaseExample, Cases.operatorWithoutPermissions)
      val expectedC592TabViewModel = Cases.c592ViewModel.map(vm => vm.copy(entryDate = Dates.format(Instant.now())))
      val expectedAttachmentsTabViewModel = Cases.attachmentsTabViewModel.map(vm => vm.copy(
        applicantFiles = Seq(Cases.storedAttachment), letter = Some(Cases.letterOfAuthority), nonApplicantFiles = Nil))
      val expectedActivityTabViewModel = ActivityViewModel.fromCase(Cases.liabilityCaseExample.copy(
        assignee = Some(Cases.operatorWithPermissions)), pagedEvent, queues)

      when(inject[FileStoreService].getAttachments(any[Case]())(any())) thenReturn (Future.successful(Seq(Cases.storedAttachment)))
      when(inject[FileStoreService].getLetterOfAuthority(any())(any())) thenReturn (Future.successful(Some(Cases.letterOfAuthority)))

      when(inject[liability_view].apply(meq(expectedLiabilityViewModel), meq(expectedC592TabViewModel), meq(expectedAttachmentsTabViewModel),
        meq(expectedActivityTabViewModel), meq(activityForm))(any(), any(), any())) thenReturn Html("body")

      val result: Future[Result] = route(app, FakeRequest("GET", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody()).get

      status(result) shouldBe OK

      verify(inject[liability_view], times(1)).apply(meq(expectedLiabilityViewModel), meq(expectedC592TabViewModel),
        meq(expectedAttachmentsTabViewModel), meq(expectedActivityTabViewModel), meq(activityForm))(any(), any(), any())
    }
  }

  "Liability controller addNote" should {
    val aCase = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions))

    "add a new note when a case note is provided" in {
      val aNote = "This is a note"

      when(inject[EventsService].addNote(meq(aCase), meq(aNote), meq(Cases.operatorWithPermissions), any[Clock])(any[HeaderCarrier])) thenReturn Future(event)

      when(inject[EventsService].getFilteredEvents(any(), any(), any())(any())) thenReturn Future(pagedEvent)

      when(inject[QueuesService].getAll) thenReturn Future(queues)

      val result: Future[Result] =
        route(app, FakeRequest("POST", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody("note" -> aNote)).get

      status(result) shouldBe SEE_OTHER

      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/v2/123456/liability")
    }

    "not add a new note when a case note is not provided" in {
      val aNote = ""

      when(inject[EventsService].addNote(meq(aCase), meq(aNote), meq(Cases.operatorWithPermissions), any[Clock])(any[HeaderCarrier])) thenReturn Future(event)

      when(inject[EventsService].getFilteredEvents(any(), any(), any())(any())) thenReturn Future(pagedEvent)

      when(inject[QueuesService].getAll) thenReturn Future(queues)

      when(inject[FileStoreService].getAttachments(any[Case]())(any())) thenReturn (Future.successful(Seq(Cases.storedAttachment)))

      when(inject[FileStoreService].getLetterOfAuthority(any())(any())) thenReturn (Future.successful(Some(Cases.letterOfAuthority)))

      when(inject[liability_view].apply(any(), any(), any(), any(), any())(any(), any(), any())) thenReturn Html("body")

      val result: Future[Result] =
        route(app, FakeRequest("POST", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody("note" -> aNote)).get

      status(result) shouldBe OK

      verify(inject[liability_view], times(1)).apply(any(), any(), any(), any(), any())(any(), any(), any())
    }
  }
}
