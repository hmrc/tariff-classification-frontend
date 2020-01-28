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

package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.tariffclassificationfrontend.controllers.ActiveTab
import uk.gov.hmrc.tariffclassificationfrontend.forms.CaseStatusRadioInputFormProvider
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.html.release_or_suppress
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withBTIApplication, withReference}

class ReleaseOrSuppressViewSpec extends ViewSpec {

  val form = new CaseStatusRadioInputFormProvider()()

  "Release or Suppress view" should {
    "contain a case heading" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c,  form, None))

      doc should containElementWithID("case-heading")
      doc.getElementById("case-heading") should containText(c.status.toString)

    }
    "contain case details" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c,  form, None))

      doc should containElementWithID("case-reference")
    }

    "contain correct radio button options" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c,  form, None))

      doc shouldNot containText(messages("change_case_status.complete"))
      doc shouldNot containText(messages("change_case_status.refer"))
      doc shouldNot containText(messages("change_case_status.reject"))
      doc shouldNot containText(messages("change_case_status.suspend"))
      doc shouldNot containText(messages("change_case_status.move_back_to_queue"))
      doc should containText(messages("change_case_status.release"))
      doc should containText(messages("change_case_status.suppress"))

    }

    "contain legend with the correct text" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, None))

      doc should containText(messages("change_case_status_legend"))
    }

    "contain correct cancel link for item tab" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, Some(ActiveTab.Item)))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference/item")
    }

    "contain correct cancel link for sample tab" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, Some(ActiveTab.Sample)))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference/sample")
    }

    "contain correct cancel link for attachments tab" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, Some(ActiveTab.Attachments)))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference/attachments")
    }

    "contain correct cancel link for activity tab" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, Some(ActiveTab.Activity)))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference/activity")
    }

    "contain correct cancel link for keywords tab" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, Some(ActiveTab.Keywords)))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference/keywords")
    }

    "contain correct cancel link for ruling tab" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, Some(ActiveTab.Ruling)))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference/ruling")
    }

    "contain correct cancel link for appeals tab" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, Some(ActiveTab.Appeals)))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference/appeal")
    }

    "contain correct cancel link for liability tab" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, Some(ActiveTab.Liability)))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference/liability")
    }

    "contain correct cancel link where tab not specified" in {
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(release_or_suppress(c, form, None))

      doc.getElementById("change_case_status-cancel_button") should haveAttribute("href","/manage-tariff-classifications/cases/reference")
    }
  }
}
