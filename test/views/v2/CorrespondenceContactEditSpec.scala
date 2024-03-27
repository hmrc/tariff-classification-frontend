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

package views.v2

import models.forms.v2.CorrespondenceContactForm
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.v2.correspondence_contact_edit

class CorrespondenceContactEditSpec extends ViewSpec {

  def correspondenceContactEdit: correspondence_contact_edit = injector.instanceOf[correspondence_contact_edit]

  private val sampleCase = Cases.correspondenceCaseExample

  "Correspondence Contact Edit View" should {

    "render h3 heading" in {
      val doc =
        view(correspondenceContactEdit(sampleCase, CorrespondenceContactForm.correspondenceContactForm(sampleCase)))

      doc.getElementById("contact-details-section-header").text shouldBe "Contact details"
      doc.getElementById("contact-address-section-header").text shouldBe "Contact address"
    }

    "render the correspondence fields correctly" in {
      val doc =
        view(correspondenceContactEdit(sampleCase, CorrespondenceContactForm.correspondenceContactForm(sampleCase)))

      doc should containElementWithID("contact-details-edit-form")
    }

    "render the correspondence view with all the fields" in {
      val doc =
        view(correspondenceContactEdit(sampleCase, CorrespondenceContactForm.correspondenceContactForm(sampleCase)))

      doc should containElementWithID("contact-details-edit-form")
      doc should containElementWithID("correspondenceStarter")
      doc should containElementWithID("name")
      doc should containElementWithID("email")
      doc should containElementWithID("phone")
      doc should containElementWithID("fax")
      doc should containElementWithID("buildingAndStreet")
      doc should containElementWithID("townOrCity")
      doc should containElementWithID("county")
      doc should containElementWithID("postCode")
      doc should containElementWithID("agentName")
    }
  }

}
