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

package controllers.v2

import com.google.inject.Provider
import controllers.{ControllerBaseSpec, RequestActions, RequestActionsWithPermissions}
import models._
import models.forms._
import models.forms.v2.LiabilityDetailsForm
import models.request.{AuthenticatedRequest, FileStoreInitiateRequest}
import models.response.{FileStoreInitiateResponse, UpscanFormTemplate}
import models.viewmodels._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.data.Form
import play.api.http.Status
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Binding, bind}
import play.api.mvc.{PlayBodyParsers, Result}
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import services._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases.{aCase, withCorrespondenceApplication, withLiabilityApplication, withReference}
import utils.{Cases, Events}
import views.html.partials.liabilities.{attachments_details, attachments_list}
import views.html.v2.{case_heading, liability_details_edit, liability_view, remove_attachment}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestActionsWithPermissionsProvider @Inject() (implicit
  parse: PlayBodyParsers,
  executionContext: ExecutionContext
) extends Provider[RequestActionsWithPermissions] {

  override def get(): RequestActionsWithPermissions =
    new RequestActionsWithPermissions(
      parse,
      Set(Permission.ADD_NOTE),
      c = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions)),
      op = Cases.operatorWithPermissions
    )
}

class LiabilityControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  val binds: List[Binding[_]] = List(
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
    bind[KeywordsService].toInstance(mock[KeywordsService]),
    bind[CasesService].toInstance(mock[CasesService])
  )

  val defaultRequestActions: List[Binding[RequestActions]] = List(
    //providers
    bind[RequestActions].toProvider[RequestActionsWithPermissionsProvider]
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      binds ++ defaultRequestActions
    )
    .configure(
      //turn off metrics
      "metrics.jvm"     -> false,
      "metrics.enabled" -> false
    )
    .build()

  private lazy val pagedEvent: Paged[Event] = Paged(Seq(Events.event), 1, 1, 1)
  private lazy val queues: List[Queue]      = List(Queue("", "", ""))
  private lazy val eventService             = mock[EventsService]
  private lazy val queueService             = mock[QueuesService]
  private lazy val liability_view           = mock[liability_view]
  private lazy val liability_details_edit   = injector.instanceOf[liability_details_edit]
  private lazy val fileStoreService         = mock[FileStoreService]
  private lazy val keywordsService          = mock[KeywordsService]
  private lazy val casesService             = mock[CasesService]

  private val keyword1: Keyword = Keyword("keyword1")
  private val keyword2: Keyword = Keyword("keyword2")

  private val initiateResponse = FileStoreInitiateResponse(
    id = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(liability_view)
    reset(eventService)
    reset(queueService)
    reset(fileStoreService)
    reset(keywordsService)
  }

  private def checkLiabilityView(timesInvoked: Int = 1): HtmlFormat.Appendable =
    verify(liability_view, times(timesInvoked)).apply(
      any[CaseViewModel],
      any[Option[C592ViewModel]],
      any[Option[RulingViewModel]],
      any[SampleStatusTabViewModel],
      any[Option[ActivityViewModel]],
      any[Form[ActivityFormData]],
      any[Option[AttachmentsTabViewModel]],
      any[Form[String]],
      any[FileStoreInitiateResponse],
      any[KeywordsTabViewModel],
      any[Form[String]],
      any[Option[AppealTabViewModel]],
      any[PrimaryNavigationTab]
    )(any[AuthenticatedRequest[_]], any[Messages])

  private def mockLiabilityController(
    pagedEvent: Paged[Event] = pagedEvent,
    queues: List[Queue] = queues,
    attachments: Seq[StoredAttachment] = Seq(Cases.storedAttachment),
    letterOfAuthority: Option[StoredAttachment] = Some(Cases.letterOfAuthority)
  ): Any = {
    when(
      eventService
        .getFilteredEvents(any[String], any[Pagination], any[Option[Set[EventType.Value]]])(any[HeaderCarrier])
    ) thenReturn Future(pagedEvent)
    when(queueService.getAll) thenReturn Future(queues)

    when(fileStoreService.getAttachments(any[Case])(any[HeaderCarrier])) thenReturn Future.successful(attachments)
    when(fileStoreService.getLetterOfAuthority(any[Case])(any[HeaderCarrier])) thenReturn Future.successful(
      letterOfAuthority
    )
    when(keywordsService.findAll()(any[HeaderCarrier])) thenReturn Future(Seq(keyword1, keyword2))
    when(keywordsService.addKeyword(any[Case], any[String], any[Operator])(any[HeaderCarrier])) thenReturn Future(
      Cases.liabilityLiveCaseExample
    )
    when(keywordsService.removeKeyword(any[Case], any[String], any[Operator])(any[HeaderCarrier])) thenReturn Future(
      Cases.liabilityLiveCaseExample
    )
    when(fileStoreService.initiate(any[FileStoreInitiateRequest])(any[HeaderCarrier])) thenReturn Future.successful(
      initiateResponse
    )

    when(
      liability_view.apply(
        any[CaseViewModel],
        any[Option[C592ViewModel]],
        any[Option[RulingViewModel]],
        any[SampleStatusTabViewModel],
        any[Option[ActivityViewModel]],
        any[Form[ActivityFormData]],
        any[Option[AttachmentsTabViewModel]],
        any[Form[String]],
        any[FileStoreInitiateResponse],
        any[KeywordsTabViewModel],
        any[Form[String]],
        any[Option[AppealTabViewModel]],
        any[PrimaryNavigationTab]
      )(any[AuthenticatedRequest[_]], any[Messages])
    ) thenReturn Html("body")
  }

  private def controller(c: Case, permissions: Set[Permission]): LiabilityController =
    new LiabilityController(
      new RequestActionsWithPermissions(
        playBodyParsers,
        permissions,
        c = c,
        op = Cases.operatorWithPermissions
      ),
      casesService,
      eventService,
      queueService,
      fileStoreService,
      keywordsService,
      mcc,
      new LiabilityDetailsForm(new CommodityCodeConstraints(), realAppConfig),
      redirectService,
      liability_view,
      liability_details_edit,
      realAppConfig
    )

  private val caseReference = "123456"

  "Calling /manage-tariff-classifications/cases/v2/:reference/liability " should {

    "return a 200 status" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())

      mockLiabilityController()

      val fakeReq                = newFakeGETRequestWithCSRF()
      val result: Future[Result] = controller(c, Set()).displayLiability(caseReference).apply(fakeReq)

      status(result) shouldBe OK

      checkLiabilityView()
    }

    "redirect to correct controller when case is different application type" in {
      val c = aCase(withReference("reference"), withCorrespondenceApplication)

      val result = await(controller(c, Set(Permission.EDIT_CORRESPONDENCE)))
        .displayLiability("reference")(newFakeGETRequestWithCSRF())
      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(
        controllers.v2.routes.CorrespondenceController.displayCorrespondence("reference").path
      )
    }
  }

  "edit Liability" should {
    "return 200 and load the editLiability form" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      mockLiabilityController()

      val fakeReq = newFakeGETRequestWithCSRF()
      val result: Future[Result] =
        controller(c, Set(Permission.EDIT_LIABILITY)).editLiabilityDetails(caseReference).apply(fakeReq)

      status(result) shouldBe OK
    }

    "return unauthorised if the user does not have the right permissions" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())

      mockLiabilityController()

      val fakeReq                = newFakeGETRequestWithCSRF()
      val result: Future[Result] = controller(c, Set()).editLiabilityDetails(caseReference).apply(fakeReq)

      status(result)             shouldBe SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "post Liability details" should {
    "redirect back to controller if the form has been submitted successfully" in {

      when(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])) thenReturn Future(
        Cases.aCaseWithCompleteDecision
      )

      val c = aCase(withReference("reference"), withLiabilityApplication())

      mockLiabilityController()
      val fakeReq = newFakePOSTRequestWithCSRF(
        Map(
          "entryDate"            -> "",
          "entryNumber"          -> "",
          "traderName"           -> "mandatory-name",
          "goodName"             -> "item-name",
          "traderCommodityCode"  -> "",
          "officerCommodityCode" -> "",
          "contact.contactName"  -> "contact-name",
          "contact.contactEmail" -> "valid@email.com",
          "contact.contactPhone" -> ""
        )
      )
      val result: Future[Result] =
        controller(c, Set(Permission.EDIT_LIABILITY)).postLiabilityDetails(caseReference).apply(fakeReq)

      status(result) shouldBe SEE_OTHER

      locationOf(result) shouldBe Some(
        "/manage-tariff-classifications/cases/v2/" + caseReference + "/liability"
      )
    }

    "return back to the view if form fails to validate" in {
      when(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])) thenReturn Future(
        Cases.aCaseWithCompleteDecision
      )
      val c = aCase(withReference("reference"), withLiabilityApplication())

      mockLiabilityController()
      val fakeReq = newFakePOSTRequestWithCSRF(
        Map(
          "entryDate"            -> "",
          "entryNumber"          -> "",
          "traderName"           -> "mandatory-name",
          "goodName"             -> "item-name",
          "traderCommodityCode"  -> "",
          "officerCommodityCode" -> "",
          "contactName"          -> "",
          "contactEmail"         -> "wrongemail",
          "contactPhone"         -> ""
        )
      )
      val result: Future[Result] =
        controller(c, Set(Permission.EDIT_LIABILITY)).postLiabilityDetails(caseReference).apply(fakeReq)

      status(result) shouldBe OK
    }
  }
}
