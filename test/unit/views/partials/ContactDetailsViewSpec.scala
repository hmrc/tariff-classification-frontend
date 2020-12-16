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

package views.partials

import models.Contact
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.contact_details

class ContactDetailsViewSpec extends ViewSpec {

  "Contact details" should {

    "Render valid email with mailto link" in {
      // Given
      val `case` = aCase(
        withReference("ref"),
        withContact(Contact("name", "email@email.com", None))
      )

      // When
      val doc = view(contact_details(`case`))

      // Then
      doc                                 should containElementWithID("contact-email")
      doc.getElementById("contact-email") should haveTag("a")
      doc.getElementById("contact-email") should haveAttribute(
        "href",
        "mailto:email@email.com?subject=ATaR application #ref"
      )
    }

    "Render invalid email as text" in {
      // Given
      val `case` = aCase(
        withReference("ref"),
        withContact(Contact("name", "email", None))
      )

      // When
      val doc = view(contact_details(`case`))

      // Then
      doc                                 should containElementWithID("contact-email")
      doc.getElementById("contact-email") should haveTag("span")
    }

    "Render valid phone" in {
      // Given
      val `case` = aCase(
        withContact(Contact("name", "email@email.com", Some("1234")))
      )

      // When
      val doc = view(contact_details(`case`))

      // Then
      doc                                     should containElementWithID("contact-telephone")
      doc.getElementById("contact-telephone") should containText("1234")
    }

    "Not render missing phone" in {
      // Given
      val `case` = aCase(
        withContact(Contact("name", "email@email.com", None))
      )

      // When
      val doc = view(contact_details(`case`))

      // Then
      doc shouldNot containElementWithID("contact-telephone")
    }

  }

}
