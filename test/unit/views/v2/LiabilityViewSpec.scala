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

package views.v2

import models.forms.{ActivityForm, ActivityFormData, KeywordForm, UploadAttachmentForm}
import models.viewmodels.{KeywordsTabViewModel, LiabilityViewModel, SampleStatusTabViewModel}
import models.{CaseStatus, Event, Paged}
import play.api.data.Form
import utils.Cases
import utils.Cases._
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.v2.liability_view

class LiabilityViewSpec extends ViewSpec {

  private val sampleStatusTabViewModel = SampleStatusTabViewModel("caseReference",
    isSampleBeingSent = true,
    Some("a person"),
    None,
    "location",
    sampleActivity = Paged.empty[Event])

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  def liabilityView: liability_view = app.injector.instanceOf[liability_view]

  def uploadAttachmentForm: Form[String] = UploadAttachmentForm.form

  def keywordForm: Form[String] = KeywordForm.form

  val emptyKeywordsTabViewModel = KeywordsTabViewModel("", Set.empty[String],Nil)

  "Liability View" should {

    "render with case reference" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc.getElementById("case-reference") should containText(c.reference)
    }

    "render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        Cases.c592ViewModel,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc should containElementWithID("c592_tab")
    }

    "not render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc shouldNot containElementWithID("c592_tab")
    }

    "render ruling tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None,
        Cases.rulingViewModel,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc should containElementWithID("ruling_tab")
    }

    "not render ruling tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc shouldNot containElementWithID("ruling_tab")
    }

    "render ruling tab when showRulingTab flag is true" in {
      val c = aCase(withReference("reference"), withLiabilityApplication()).copy(status = CaseStatus.OPEN)
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None,
        Cases.rulingViewModel,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc should containElementWithID("ruling_tab")
    }

    "not render ruling tab when showRulingTab flag is false" in {
      val c = aCase(withReference("reference"), withLiabilityApplication()).copy(status = CaseStatus.CANCELLED)
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None,
        Cases.rulingViewModel,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc shouldNot containElementWithID("ruling_tab")
    }

    "render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment),
        None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel, activityForm,
        Cases.attachmentsTabViewModel.map(_.copy(attachments = Seq(Cases.storedAttachment), letter = Some(Cases.letterOfAuthority))),
        UploadAttachmentForm.form,
        emptyKeywordsTabViewModel,
        keywordForm
      ))
      doc should containElementWithID("attachments_tab")
    }

    "not render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc shouldNot containElementWithID("attachments_tab")
    }

    "render Activity tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment),
        None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc should containElementWithID("activity_tab")
    }

    "render Sample tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())

      val doc = view(liabilityView(LiabilityViewModel.fromCase(
        c, Cases.operatorWithAddAttachment), None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm
      ))
      doc should containElementWithID("sample_status_tab")
    }

    "render Keywords tab when showRulingAndKeywords is true" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication()).copy(status = CaseStatus.OPEN)
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment),
        None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel, activityForm,
        Cases.attachmentsTabViewModel.map(_.copy(attachments = Seq(Cases.storedAttachment), letter = Some(Cases.letterOfAuthority))),
        UploadAttachmentForm.form,
        emptyKeywordsTabViewModel,
        keywordForm
      ))
      doc should containElementWithID("keywords_tab")
    }

    "not render Keywords tab when showRulingAndKeywords is false" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication()).copy(status = CaseStatus.NEW)
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None,
        None,
        sampleStatusTabViewModel,
        Cases.activityTabViewModel,
        activityForm,
        None,
        uploadAttachmentForm,
        emptyKeywordsTabViewModel,
        keywordForm)
      )
      doc shouldNot containElementWithID("keywords_tab")
    }

  }
}
