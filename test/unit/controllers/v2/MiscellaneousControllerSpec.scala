/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.{ControllerBaseSpec, RequestActionsWithPermissions, SuccessfulRequestActions}
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.twirl.api.Html
import service.{EventsService, FileStoreService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.Cases.{aCase, pagedEvent, withMiscellaneousApplication, withReference}
import views.html.v2.miscellaneous_view

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MiscellaneousControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {
  private val queueService                       = mock[QueuesService]
  private val eventService                       = mock[EventsService]
  private val fileService                        = mock[FileStoreService]
  private val operator                           = Operator(id = "id")
  private val event                              = mock[Event]
  private val miscellaneousView                  = mock[miscellaneous_view]
  private val attachments: Seq[StoredAttachment] = Seq(Cases.storedAttachment)
  private lazy val queues: List[Queue]           = List(Queue("", "", ""))

  override protected def beforeEach(): Unit =
    reset(
      queueService,
      fileService,
      event,
      miscellaneousView
    )

  private def controller(c: Case) = new MiscellaneousController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = c),
    eventService,
    queueService,
    fileService,
    mcc,
    miscellaneousView,
    realAppConfig
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
      when(queueService.getAll) thenReturn Future(queues)

      when(
        miscellaneousView.apply(
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any()
        )(any(), any(), any())
      ) thenReturn Html("body")

      val result = await(controller(c, Set(Permission.EDIT_CORRESPONDENCE)))
        .displayMiscellaneous("reference")(newFakeGETRequestWithCSRF(app))
      status(result) shouldBe Status.OK
    }

  }
}
