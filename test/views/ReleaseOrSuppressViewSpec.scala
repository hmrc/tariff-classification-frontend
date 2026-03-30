/*
 * Copyright 2025 HM Revenue & Customs
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

package views

import models.forms.CaseStatusRadioInputFormProvider
import utils.Cases.{aCase, withBTIApplication, withReference}
import views.ViewMatchers._
import views.html.release_or_suppress

class ReleaseOrSuppressViewSpec extends ViewSpec {

  val form                                       = new CaseStatusRadioInputFormProvider()()
  val releaseOrSuppressView: release_or_suppress = app.injector.instanceOf[release_or_suppress]

  "Release or Suppress view" should {
    "contain a case heading" in {
      val c   = aCase(withReference("reference"), withBTIApplication)
      val doc = view(releaseOrSuppressView(c, form))

      doc                                should containElementWithID("case-heading")
      doc.getElementById("case-heading") should containText(c.status.toString)

    }
    "contain case details" in {
      val c   = aCase(withReference("reference"), withBTIApplication)
      val doc = view(releaseOrSuppressView(c, form))

      doc should containElementWithID("case-reference")
    }

    "contain correct radio button options" in {
      val c   = aCase(withReference("reference"), withBTIApplication)
      val doc = view(releaseOrSuppressView(c, form))

      doc shouldNot containText(messages("change_case_status.complete"))
      doc shouldNot containText(messages("change_case_status.refer"))
      doc shouldNot containText(messages("change_case_status.reject"))
      doc shouldNot containText(messages("change_case_status.suspend"))
      doc shouldNot containText(messages("change_case_status.move_back_to_queue"))
      doc should containText(messages("change_case_status.release"))
      doc should containText(messages("change_case_status.suppress"))

    }

    "contain legend with the correct text" in {
      val c   = aCase(withReference("reference"), withBTIApplication)
      val doc = view(releaseOrSuppressView(c, form))

      doc should containText(messages("action_this_case_status_legend"))
    }

    "contain correct cancel link" in {
      val c   = aCase(withReference("reference"), withBTIApplication)
      val doc = view(releaseOrSuppressView(c, form))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute(
        "href",
        "/manage-tariff-classifications/cases/reference"
      )
    }
  }
}
