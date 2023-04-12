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

package views.partials

import models.Contact
import models.viewmodels.atar.ApplicantTabViewModel
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.contact_details

class ContactDetailsViewSpec extends ViewSpec {

  "Contact details" should {

    "Render valid email with mailto link" in {

      val `case` = aCase(
        withReference("ref"),
        withContact(Contact("name", "email@email.com", None))
      )

      val applicantTab = ApplicantTabViewModel.fromCase(`case`, Map.empty)


      val doc = view(contact_details(applicantTab))


      doc                                 should containElementWithID("contact-email")
      doc.getElementById("contact-email") should haveTag("a")
      doc.getElementById("contact-email") should haveAttribute(
        "href",
        "mailto:email@email.com?subject=ATaR%20Application%20#ref"
      )
    }

    "Render invalid email as text" in {

      val `case` = aCase(
        withReference("ref"),
        withContact(Contact("name", "email", None))
      )

      val applicantTab = ApplicantTabViewModel.fromCase(`case`, Map.empty)


      val doc = view(contact_details(applicantTab))


      doc                                 should containElementWithID("contact-email")
      doc.getElementById("contact-email") should haveTag("span")
    }

    "Render valid phone" in {

      val `case` = aCase(
        withContact(Contact("name", "email@email.com", Some("1234")))
      )

      val applicantTab = ApplicantTabViewModel.fromCase(`case`, Map.empty)


      val doc = view(contact_details(applicantTab))


      doc                                     should containElementWithID("contact-telephone")
      doc.getElementById("contact-telephone") should containText("1234")
    }

    "Not render missing phone" in {

      val `case` = aCase(
        withContact(Contact("name", "email@email.com", None))
      )

      val applicantTab = ApplicantTabViewModel.fromCase(`case`, Map.empty)


      val doc = view(contact_details(applicantTab))


      doc shouldNot containElementWithID("contact-telephone")
    }

  }

}
