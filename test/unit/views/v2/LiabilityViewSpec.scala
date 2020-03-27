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

import models.viewmodels.LiabilityViewModel
import utils.Cases
import utils.Cases.{aCase, withLiabilityApplication, withReference}
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.v2.liability_view

class LiabilityViewSpec extends ViewSpec {

  def liabilityView: liability_view = app.injector.instanceOf[liability_view]

  "Liability View" should {

    "render with case reference" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None))
      doc.getElementById("case-reference") should containText(c.reference)
    }

    "render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), Cases.c592ViewModel, None))
      doc should containElementWithID("c592_tab")
    }

    "not render C592 tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None))
      doc shouldNot containElementWithID("c592_tab")
    }

    "render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, Cases.attachmentsTabViewModel))
      doc should containElementWithID("attachments_tab")
    }

    "not render Attachments tab" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions), None, None))
      doc shouldNot containElementWithID("attachments_tab")
    }

  }
}
