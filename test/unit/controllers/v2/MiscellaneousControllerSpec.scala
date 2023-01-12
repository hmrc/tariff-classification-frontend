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

package controllers.v2

import config.AppConfig
import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import models._
import models.forms._
import models.request.{AuthenticatedRequest, FileStoreInitiateRequest}
import models.response.{FileStoreInitiateResponse, UpscanFormTemplate}
import models.viewmodels._
import models.viewmodels.miscellaneous._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.i18n.Messages
import play.twirl.api.Html
import service.{EventsService, FileStoreService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases, Cases._
import views.html.v2.miscellaneous_view

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MiscellaneousControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {
  private val queueService                       = mock[QueuesService]
  private val eventService                       = mock[EventsService]
  private val fileService                        = mock[FileStoreService]
  private val event                              = mock[Event]
  private val miscellaneousView                  = mock[miscellaneous_view]
  private val attachments: Seq[StoredAttachment] = Seq(Cases.storedAttachment)
  private lazy val queues: List[Queue]           = List(Queue("", "", ""))
  private val initiateResponse = FileStoreInitiateResponse(
    id              = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  override protected def beforeEach(): Unit =
    reset(
      queueService,
      fileService,
      event,
      miscellaneousView
    )

  private def controller(c: Case, permission: Set[Permission]) = new MiscellaneousController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = c),
    eventService,
    queueService,
    fileService,
    mcc,
    miscellaneousView,
    realAppConfig
  )

  "Miscellaneous Controller" should {
    "display miscellaneous case" in {
      val c = aCase(withReference("reference"), withMiscellaneousApplication)
      when(fileService.getAttachments(any[Case])(any[HeaderCarrier])) thenReturn (Future.successful(attachments))

      when(
        eventService
          .getFilteredEvents(any[String], any[Pagination], any[Option[Set[EventType.Value]]])(any[HeaderCarrier])
      ) thenReturn Future(pagedEvent)

      when(fileService.initiate(any[FileStoreInitiateRequest])(any[HeaderCarrier])) thenReturn Future.successful(
        initiateResponse
      )

      when(queueService.getAll) thenReturn Future(queues)

      when(
        miscellaneousView(
          any[CaseViewModel],
          any[DetailsViewModel],
          any[MessagesTabViewModel],
          any[Form[MessageFormData]],
          any[SampleStatusTabViewModel],
          any[atar.AttachmentsTabViewModel],
          any[Form[String]],
          any[FileStoreInitiateResponse],
          any[ActivityViewModel],
          any[Form[ActivityFormData]],
          any[Seq[StoredAttachment]],
          any[PrimaryNavigationTab]
        )(any[AuthenticatedRequest[_]], any[Messages], any[AppConfig])
      ) thenReturn Html("body")

      val result = await(controller(c, Set(Permission.EDIT_CORRESPONDENCE)))
        .displayMiscellaneous("reference")(newFakeGETRequestWithCSRF())
      status(result) shouldBe Status.OK
    }

  }
}
