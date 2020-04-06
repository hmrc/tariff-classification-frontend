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

import models.CaseStatus
import models.forms.{ActivityForm, ActivityFormData, UploadAttachmentForm}
import models.viewmodels.LiabilityViewModel
import play.api.data.Form
import utils.Cases
import utils.Cases._
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.v2.liability_view

class LiabilityViewSpec extends ViewSpec {

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  def liabilityView: liability_view = app.injector.instanceOf[liability_view]

  def uploadAttachmentForm: Form[String] = UploadAttachmentForm.form

  "Liability View" should {

    "render with case reference" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))
      doc.getElementById("case-reference") should containText(c.reference)
    }

    "render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), Cases.c592ViewModel, None, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))
      doc should containElementWithID("c592_tab")
    }

    "not render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))
      doc shouldNot containElementWithID("c592_tab")
    }

    "render ruling tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, Cases.rulingViewModel, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))
      doc should containElementWithID("ruling_tab")
    }

    "not render ruling tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))
      doc shouldNot containElementWithID("ruling_tab")
    }

    "render ruling tab when showRulingTab flag is true" in {
      val c = aCase(withReference("reference"), withLiabilityApplication()).copy(status = CaseStatus.OPEN)
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, Cases.rulingViewModel, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))
      doc should containElementWithID("ruling_tab")
    }

    "not render ruling tab when showRulingTab flag is false" in {
      val c = aCase(withReference("reference"), withLiabilityApplication()).copy(status = CaseStatus.CANCELLED)
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, Cases.rulingViewModel, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))
      doc shouldNot containElementWithID("ruling_tab")
    }

    "render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment),
        None,
        None,
        Cases.activityTabViewModel, activityForm,
        Cases.attachmentsTabViewModel.map(_.copy(attachments = Seq(Cases.storedAttachment), letter = Some(Cases.letterOfAuthority))),
        UploadAttachmentForm.form
      ))
      doc should containElementWithID("attachments_tab")
    }

    "not render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))
      doc shouldNot containElementWithID("attachments_tab")
    }

    "render Activity tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment), None, None,
        Cases.activityTabViewModel, activityForm,
        None, uploadAttachmentForm
      ))
      doc should containElementWithID("activity_tab")
    }

    "render the action this case button for new case" in {

      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())

      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithReleaseOrSuppressPermissions),
        None, None, None, activityForm, None, uploadAttachmentForm))

      doc should containElementWithID("action-this-case-button")
    }

    "not render the action this case button" in {

      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())

      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None, None, Cases.activityTabViewModel, activityForm, None, uploadAttachmentForm))

      doc shouldNot containElementWithID("action-this-case-button")
    }
  }
}
