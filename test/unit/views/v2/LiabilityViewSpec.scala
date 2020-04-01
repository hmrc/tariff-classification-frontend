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

import java.time.Instant

import controllers.ActiveTab
import models.forms.{ActivityForm, ActivityFormData}
import models.{Event, Operator, Permission}
import models.request.AuthenticatedRequest
import models.viewmodels.LiabilityViewModel
import org.jsoup.select.Elements
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Request
import play.twirl.api.Html
import utils.{Cases, Events}
import utils.Cases.{aCase, withBTIApplication, withLiabilityApplication, withReference}
import views.ViewMatchers.{containElementWithID, containText, haveAttribute}
import views.{CaseDetailPage, ViewSpec, html}
import utils.Cases
import utils.Cases.{aCase, withLiabilityApplication, withReference}
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.v2.liability_view
import models.{Case, Event, Paged, Permission, Queue}
import play.api.data.Form

class LiabilityViewSpec extends ViewSpec {

  def liabilityView: liability_view = app.injector.instanceOf[liability_view]

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  private val pagedEvent = Paged(Seq(Events.event), 1, 1, 1)

  "Liability View" should {

    "render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        Cases.c592ViewModel, None, Cases.activityTabViewModel, activityForm))

      doc should containElementWithID("c592_tab")
    }

    "not render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None, Cases.activityTabViewModel, activityForm))
      doc shouldNot containElementWithID("c592_tab")
    }

    "render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(
        LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment),
        None,
        Cases.attachmentsTabViewModel.map(_.copy(applicantFiles = Seq(Cases.storedAttachment),
          letter = Some(Cases.letterOfAuthority),
          nonApplicantFiles = Seq(Cases.storedOperatorAttachment))), Cases.activityTabViewModel, activityForm))
      doc should containElementWithID("attachments_tab")
    }

    "not render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None, Cases.activityTabViewModel, activityForm))
      doc shouldNot containElementWithID("attachments_tab")
    }

    "render Activity tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithAddAttachment), None,
        Cases.attachmentsTabViewModel.map(_.copy(applicantFiles = Seq(Cases.storedAttachment),
          letter = Some(Cases.letterOfAuthority),
          nonApplicantFiles = Seq(Cases.storedOperatorAttachment))), Cases.activityTabViewModel, activityForm))
      doc should containElementWithID("activity_tab")
    }
  }
}
