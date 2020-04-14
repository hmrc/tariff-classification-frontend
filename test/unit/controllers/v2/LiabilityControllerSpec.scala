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
import controllers.{ControllerBaseSpec, RequestActions, RequestActionsWithPermissions}
import javax.inject.Inject
import models.forms.{ActivityForm, ActivityFormData}
import models.{Case, _}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, _}
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.data.Form
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{BodyParsers, Result}
import play.api.mvc.BodyParsers.Default
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.BodyParser.Default
import play.twirl.api.Html
import service.{EventsService, FileStoreService, KeywordsService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Cases, Events}
import views.html.partials.liabilities.{attachments_details, attachments_list}
import views.html.v2.{case_heading, liability_view, remove_attachment}

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

  val binds = List(

    //views
    bind[liability_view].toInstance(mock[liability_view]),
    bind[EventsService].toInstance(mock[EventsService]),
    bind[QueuesService].toInstance(mock[QueuesService]),
    bind[case_heading].toInstance(mock[case_heading]),
    bind[attachments_details].toInstance(mock[attachments_details]),
    bind[remove_attachment].toInstance(mock[remove_attachment]),
    bind[attachments_list].toInstance(mock[attachments_list]),
    //services
    bind[FileStoreService].toInstance(mock[FileStoreService]),
    bind[KeywordsService].toInstance(mock[KeywordsService])
  )

  val defaultRequestActions = List(
    //providers
    bind[RequestActions].toProvider[RequestActionsWithPermissionsProvider])

  val requestActionsWithKeywordsPermissionBinder = List(
    bind[RequestActions].to(requestActionsWithKeywordsPermission)
  )

  val requestActionsWithKeywordsPermission:  RequestActionsWithPermissions = {
    new RequestActionsWithPermissions(
      inject[BodyParsers.Default], Set(Permission.KEYWORDS),
      c = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions)),
      op = Cases.operatorWithPermissions
    )
  }

  val appWithKeywordPermissions: Application = new GuiceApplicationBuilder().overrides(
    binds ++ requestActionsWithKeywordsPermissionBinder
  ).configure(
    "metrics.jvm" -> false,
    "metrics.enabled" -> false,
    "new-liability-details" -> true
  ).build()

  override lazy val app: Application = new GuiceApplicationBuilder().overrides(
   binds ++ defaultRequestActions
  ).configure(
    "metrics.jvm" -> false,
    "metrics.enabled" -> false,
    "new-liability-details" -> true
  ).build()
  
  private val pagedEvent: Paged[Event] = Paged(Seq(Events.event), 1, 1, 1)
  private val queues: Seq[Queue] = Seq(Queue("", "", ""))
  private val event = mock[Event]

  override def beforeEach(): Unit =
    reset(
      inject[liability_view],
      inject[EventsService],
      inject[QueuesService],
      inject[FileStoreService],
      inject[KeywordsService]
    )

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def checkLiabilityView(timesInvoked: Int) =
    verify(inject[liability_view], times(timesInvoked)).apply(
      any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
    )(any(), any(), any())

  private def mockLiabilityController(
                                       pagedEvent: Paged[Event] = pagedEvent,
                                       queues: Seq[Queue] = queues,
                                       attachments: Seq[StoredAttachment] = Seq(Cases.storedAttachment),
                                       letterOfAuthority: Option[StoredAttachment] = Some(Cases.letterOfAuthority)
                                     ): Any = {
    when(inject[EventsService].getFilteredEvents(any(), any(), any())(any())) thenReturn Future(pagedEvent)
    when(inject[QueuesService].getAll) thenReturn Future(queues)

    when(inject[FileStoreService].getAttachments(any[Case]())(any())) thenReturn (Future.successful(attachments))
    when(inject[FileStoreService].getLetterOfAuthority(any())(any())) thenReturn (Future.successful(letterOfAuthority))
    when(inject[KeywordsService].autoCompleteKeywords) thenReturn Future(Seq("keyword1", "keyword2"))
    when(inject[KeywordsService].addKeyword(any(), any(), any())(any())) thenReturn Future(Cases.liabilityLiveCaseExample)
    mockLiabilityView
  }

  private def mockLiabilityView =
    when(inject[liability_view].apply(
      any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
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

  "Liability add keyword" should {

    "redirect back to display liability if form submitted successfully" in {

      when(inject[KeywordsService].addKeyword(
        meq(Cases.liabilityCaseExample), meq("pajamas"), meq(Cases.operatorWithKeywordsPermissions))
      (any[HeaderCarrier])) thenReturn Future(Cases.liabilityCaseExample)

      mockLiabilityController()

      val result: Future[Result] =
        route(appWithKeywordPermissions, FakeRequest("POST", "/manage-tariff-classifications/cases/v2/123456/liability/add-keyword")
          .withFormUrlEncodedBody("keyword" -> "pajamas")).get

      status(result) shouldBe 303
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/v2/123456/liability#keywords_tab")
    }

    "redirect to unauthorised if the user does not have the right permissions" in {

      val result: Future[Result] =
        route(app, FakeRequest("POST", "/manage-tariff-classifications/cases/v2/123456/liability/add-keyword")
          .withFormUrlEncodedBody("keyword" -> "pajamas")).get

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }
}
