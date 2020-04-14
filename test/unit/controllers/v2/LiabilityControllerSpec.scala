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

import com.google.inject.Provider
import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import javax.inject.Inject
import models.{Case, _}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, _}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import service.{EventsService, FileStoreService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Cases, Events}
import views.html.v2.liability_view

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

class LiabilityControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {
  private val pagedEvent: Paged[Event] = Paged(Seq(Events.event), 1, 1, 1)
  private val queues: Seq[Queue] = Seq(Queue("", "", ""))
  private val event = mock[Event]

  override def beforeEach(): Unit =
    reset(
      inject[liability_view],
      inject[EventsService],
      inject[QueuesService],
      inject[FileStoreService]
    )

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def checkLiabilityView(timesInvoked: Int) =
    verify(inject[liability_view], times(timesInvoked)).apply(
      any(), any(), any(), any(), any(), any(), any(), any()
    )(any(), any(), any())

  private def mockLiabilityController(
                                       pagedEvent: Paged[Event] = pagedEvent,
                                       queues: Seq[Queue] = queues,
                                       attachments: Seq[StoredAttachment] = Seq(Cases.storedAttachment),
                                       letterOfAuthority: Option[StoredAttachment] = Some(Cases.letterOfAuthority)
                                     ): Any = {
    when(inject[EventsService].getFilteredEvents(any(), any(), any())(any())) thenReturn Future(pagedEvent)
    when(inject[QueuesService].getAll) thenReturn Future(queues)

    when(inject[FileStoreService].getAttachments(any[Case]())(any())) thenReturn Future.successful(attachments)
    when(inject[FileStoreService].getLetterOfAuthority(any())(any())) thenReturn Future.successful(letterOfAuthority)

    mockLiabilityView
  }

  private def mockLiabilityView =
    when(inject[liability_view].apply(
      any(), any(), any(), any(), any(), any(), any(), any()
    )(any(), any(), any())) thenReturn Html("body")


  "Calling /manage-tariff-classifications/cases/v2/:reference/liability " should {

    "return a 200 status" in {
      mockLiabilityController()

      val fakeReq = FakeRequest("GET", "/manage-tariff-classifications/cases/v2/123456/liability")
      val result: Future[Result] = route(app, fakeReq).get

      status(result) shouldBe OK

      checkLiabilityView(1)
    }
  }

  "Liability controller addNote" should {
    val aCase = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions))

    "add a new note when a case note is provided" in {
      val aNote = "This is a note"

      when(inject[EventsService].addNote(meq(aCase), meq(aNote), meq(Cases.operatorWithPermissions), any[Clock])(any[HeaderCarrier])) thenReturn Future(event)

      mockLiabilityController()

      val result: Future[Result] =
        route(app, FakeRequest("POST", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody("note" -> aNote)).get

      status(result) shouldBe SEE_OTHER

      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/v2/123456/liability")
    }

    "not add a new note when a case note is not provided" in {
      val aNote = ""

      when(inject[EventsService].addNote(meq(aCase), meq(aNote), meq(Cases.operatorWithPermissions), any[Clock])(any[HeaderCarrier])) thenReturn Future(event)

      mockLiabilityController()

      val result: Future[Result] =
        route(app, FakeRequest("POST", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody("note" -> aNote)).get

      status(result) shouldBe OK

      checkLiabilityView(1)
    }
  }
}
