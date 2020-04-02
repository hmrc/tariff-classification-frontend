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

import models.forms.{ActivityForm, ActivityFormData}
import models.viewmodels.LiabilityViewModel
import models.{CaseStatus, Paged}
import play.api.data.Form
import utils.Cases.{aLiabilityCase, withLiabilityApplication, withReference, withStatus}
import utils.{Cases, Events}
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.v2.liability_view

class LiabilityViewSpec extends ViewSpec {

  def liabilityView: liability_view = app.injector.instanceOf[liability_view]

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  "Liability View" should {

    "render the action this case button for new case" in {

      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())

      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithReleaseOrSuppressPermissions),
        None, None, Cases.activityTabViewModel, activityForm))

      doc should containElementWithID("action-this-case-button")
    }

    "not render the action this case button" in {

      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.NEW) , withLiabilityApplication())

      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        None, None, Cases.activityTabViewModel, activityForm))

      doc shouldNot containElementWithID("action-this-case-button")
    }

    "render C592 tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        Cases.c592ViewModel, None, Cases.activityTabViewModel, activityForm))

      doc should containElementWithID("c592_tab")
    }

    "not render C592 tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None, Cases.activityTabViewModel, activityForm))
      doc shouldNot containElementWithID("c592_tab")
    }

    "render Attachments tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment),
        None,
        Cases.attachmentsTabViewModel.map(_.copy(applicantFiles = Seq(Cases.storedAttachment),
          letter = Some(Cases.letterOfAuthority),
          nonApplicantFiles = Seq(Cases.storedOperatorAttachment))), Cases.activityTabViewModel, activityForm))
      doc should containElementWithID("attachments_tab")
    }

    "not render Attachments tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None, Cases.activityTabViewModel, activityForm))
      doc shouldNot containElementWithID("attachments_tab")
    }

    "render Activity tab" in {
      val c = aLiabilityCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment), None,
        Cases.attachmentsTabViewModel.map(_.copy(applicantFiles = Seq(Cases.storedAttachment),
          letter = Some(Cases.letterOfAuthority),
          nonApplicantFiles = Seq(Cases.storedOperatorAttachment))), Cases.activityTabViewModel, activityForm))
      doc should containElementWithID("activity_tab")
    }
  }
}
