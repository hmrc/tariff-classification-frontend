/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Contact}
import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.{application_details, case_trader}
import uk.gov.tariffclassificationfrontend.utils.Cases
import uk.gov.tariffclassificationfrontend.utils.Cases._

class CaseTraderViewSpec extends ViewSpec {

  "Case Trader" should {

    "Not render agent details when not present" in {
      // Given
      val `case` = aCase(
        withReference("ref"),
        withoutAgent()
      )

      // When
      val doc = view(case_trader(`case`, None))

      // Then
      doc shouldNot containElementWithID("agent-submitted-heading")
    }

    "Render agent details when present" in {
      // Given
      val `case` = aCase(
        withReference("ref"),
        withAgent()
      )

      // When
      val doc = view(case_trader(`case`, None))

      // Then
      doc should containElementWithID("agent-submitted-heading")
    }

    "Render valid email with mailto link" in {
      // Given
      val `case` = aCase(
        withReference("ref"),
        withContact(Contact("name", "email@email.com", None))
      )

      // When
      val doc = view(case_trader(`case`, None))

      // Then
      doc should containElementWithID("contact-email")
      doc.getElementById("contact-email") should haveTag("a")
      doc.getElementById("contact-email") should haveAttribute("href", "mailto:email@email.com?subject=BTI application #ref")
    }

    "Render invalid email as text" in {
      // Given
      val `case` = aCase(
        withReference("ref"),
        withContact(Contact("name", "email", None))
      )

      // When
      val doc = view(case_trader(`case`, None))

      // Then
      doc should containElementWithID("contact-email")
      doc.getElementById("contact-email") should haveTag("span")
    }

    "Render valid phone" in {
      // Given
      val `case` = aCase(
        withContact(Contact("name", "email@email.com", Some("1234")))
      )

      // When
      val doc = view(case_trader(`case`, None))

      // Then
      doc should containElementWithID("contact-phone")
      doc.getElementById("contact-phone") should containText("1234")
    }

    "Not render missing phone" in {
      // Given
      val `case` = aCase(
        withContact(Contact("name", "email@email.com", None))
      )

      // When
      val doc = view(case_trader(`case`, None))

      // Then
      doc shouldNot containElementWithID("contact-phone")
    }

    "Render letter of Authority when present as link" in {
      // Given
      val `case` = aCase(
        withReference("ref"),
        withContact(Contact("name", "email", None))
      )

      val letterOfAuthorization = Cases.storedAttachment.copy(id = "letter-of-auth-id", url = Some("url"), scanStatus = Some(ScanStatus.READY))

      // When
      val doc = view(case_trader(`case`, Some(letterOfAuthorization)))

      // Then
      doc should containElementWithID("agent-letter-file-letter-of-auth-id")
      doc.getElementById("agent-letter-file-letter-of-auth-id") should containText("View letter of authorisation")
    }

    "show no tag when no letter of auth is provided" in {

      // Given When
      val doc = view(case_trader(btiCaseExample, None))

      // Then
      doc shouldNot containElementWithID("agent-letter-file-letter-of-auth-id")
    }

    "show the suppress case section for NEW cases" in {
      // Given
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.NEW)

      // When
      val doc = view(case_trader(case1, None))

      // Then
      doc should containText("Releasing or suppressing a case")
    }

    "not show the suppress case section for non-NEW cases" in {
      // Given
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

      // When
      val doc = view(case_trader(case1, None))

      // Then
      doc shouldNot containText("Releasing or suppressing a case")
    }

  }

}
