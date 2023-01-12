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

package views.v2

import models._
import models.forms._
import models.response.{FileStoreInitiateResponse, UpscanFormTemplate}
import models.viewmodels._
import models.request.AuthenticatedRequest
import play.api.data.Form
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}
import utils.Cases
import utils.Cases._
import utils.Notification._
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.v2.liability_view
import play.api.test.CSRFTokenHelper._

class LiabilityViewSpec extends ViewSpec {

  private val sampleStatusTabViewModel = SampleStatusTabViewModel(
    "caseReference",
    isSampleBeingSent = true,
    Some(SampleSend.AGENT),
    None,
    "location",
    sampleActivity = Paged.empty[Event]
  )

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  def liabilityView: liability_view = injector.instanceOf[liability_view]

  def uploadAttachmentForm: Form[String] = UploadAttachmentForm.form

  val initiateResponse = FileStoreInitiateResponse(
    id              = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  def keywordForm: Form[String] = KeywordForm.form

  val emptyKeywordsTabViewModel: KeywordsTabViewModel = KeywordsTabViewModel("", Set.empty[String], Nil)

  val appealTabViewModel = Some(
    AppealTabViewModel(
      caseReference = "123456",
      appeals       = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1)),
      Some(""),
      false
    )
  )

  "Liability View" should {

    "render with case reference" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc.getElementById("case-reference") should containText(c.reference)
    }

    "render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          Some(Cases.c592ViewModel),
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc should containElementWithID("c592_tab")
    }

    "not render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc shouldNot containElementWithID("c592_tab")
    }

    "render ruling tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          None,
          Cases.rulingViewModel,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc should containElementWithID("ruling_tab")
    }

    "not render ruling tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc shouldNot containElementWithID("ruling_tab")
    }

    "render ruling tab when showRulingTab flag is true" in {
      val c = aCase(withReference("reference"), withLiabilityApplication()).copy(status = CaseStatus.OPEN)
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          None,
          Cases.rulingViewModel,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc should containElementWithID("ruling_tab")
    }

    "not render ruling tab when showRulingTab flag is false" in {
      val c = aCase(withReference("reference"), withLiabilityApplication()).copy(status = CaseStatus.CANCELLED)
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          None,
          Cases.rulingViewModel,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc shouldNot containElementWithID("ruling_tab")
    }

    "render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithAddAttachment),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          Cases.attachmentsTabViewModel
            .map(_.copy(attachments = Seq(Cases.storedAttachment), letter = Some(Cases.letterOfAuthority))),
          UploadAttachmentForm.form,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc should containElementWithID("attachments_tab")
    }

    "not render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc shouldNot containElementWithID("attachments_tab")
    }

    "render Activity tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithAddAttachment),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc should containElementWithID("activity_tab")
    }

    "render Sample tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())

      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithAddAttachment),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )
      doc should containElementWithID("sample_status_tab")
    }

    "render Appeals tab" in {

      val c  = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.COMPLETED))
      val op = Cases.operatorWithPermissions.copy(permissions = Set(Permission.APPEAL_CASE))

      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, op),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )

      doc should containElementWithID("appeal_tab")
    }

    "not render Appeals tab" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.OPEN))

      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithPermissions),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )
      )

      doc shouldNot containElementWithID("appeal_tab")
    }

    "render notification banner when note was added" in {

      val requestWithFlashKeywordSuccess: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest("GET", "/", FakeHeaders(Seq("csrfToken" -> "csrfToken")), AnyContentAsEmpty)
          .withFlash(success("case.activity.note.success.text"))
          .withCSRFToken
          .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(
        liabilityView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          None,
          None,
          sampleStatusTabViewModel,
          Cases.activityTabViewModel,
          activityForm,
          None,
          uploadAttachmentForm,
          initiateResponse,
          emptyKeywordsTabViewModel,
          keywordForm,
          appealTabViewModel
        )(
          AuthenticatedRequest(
            authenticatedOperator,
            requestWithFlashKeywordSuccess
          ),
          messages,
          appConfig
        )
      )

      doc should containElementWithID("govuk-notification-banner-title")

    }
  }
}
