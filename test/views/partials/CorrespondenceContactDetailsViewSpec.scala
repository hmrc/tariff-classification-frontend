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

package views.partials

import models.Permission
import models.viewmodels.correspondence.ContactDetailsTabViewModel
import utils.Cases.aCorrespondenceCase
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.partials.correspondence_contact_details

class CorrespondenceContactDetailsViewSpec extends ViewSpec {

  "Correspondence Contact Details" should {

    "render edit contact details when user has permission to edit correspondence" in {
      val c              = aCorrespondenceCase()
      val caseDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(
        correspondence_contact_details(caseDetailsTab)(
          requestWithPermissions(Permission.EDIT_CORRESPONDENCE),
          messages,
          appConfig
        )
      )

      val editCorrespondence = doc.getElementById("edit-contact-details")
      editCorrespondence.text() shouldBe "Edit contact details"
    }

    "not render edit contact details when user does not have permission to edit" in {
      val c              = aCorrespondenceCase()
      val caseDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(correspondence_contact_details(caseDetailsTab))

      doc shouldNot containElementWithID("edit-contact-details")
    }

    "render correspondence starter when present" in {
      val c                 = aCorrespondenceCase()
      val contactDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(correspondence_contact_details(contactDetailsTab))

      val summary = doc.getElementById("correspondence-starter")
      summary.text() shouldBe "Starter"
    }

    "render contact name when present" in {
      val c                 = aCorrespondenceCase()
      val contactDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(correspondence_contact_details(contactDetailsTab))

      val summary = doc.getElementById("contact-name")
      summary.text() shouldBe "a name"
    }

    "render contact email when present" in {
      val c                 = aCorrespondenceCase()
      val contactDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(correspondence_contact_details(contactDetailsTab))

      val summary = doc.getElementById("contact-email")
      summary.text() shouldBe "anemail@some.com"
    }

    "render contact phone when present" in {
      val c                 = aCorrespondenceCase()
      val contactDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(correspondence_contact_details(contactDetailsTab))

      val summary = doc.getElementById("contact-phone")
      summary.text() shouldBe ""
    }

    "render contact fax when present" in {
      val c                 = aCorrespondenceCase()
      val contactDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(correspondence_contact_details(contactDetailsTab))

      val summary = doc.getElementById("contact-fax")
      summary.text() shouldBe ""
    }

    "render contact address when present" in {
      val c                 = aCorrespondenceCase()
      val contactDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(correspondence_contact_details(contactDetailsTab))

      val summary = doc.getElementById("contact-address")
      summary.text() shouldBe "New building Old Town"
    }

    "render agent name when present" in {
      val c                 = aCorrespondenceCase()
      val contactDetailsTab = ContactDetailsTabViewModel.fromCase(c)

      val doc = view(correspondence_contact_details(contactDetailsTab))

      val summary = doc.getElementById("agent-name")
      summary.text() shouldBe "Agent 007"
    }

  }
}
