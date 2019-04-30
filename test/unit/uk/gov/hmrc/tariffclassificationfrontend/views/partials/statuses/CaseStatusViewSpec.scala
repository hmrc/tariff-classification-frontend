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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.statuses

import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CANCELLED
import uk.gov.hmrc.tariffclassificationfrontend.models.{CancelReason, Cancellation}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.statuses.case_status
import uk.gov.tariffclassificationfrontend.utils.Cases
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withDecision, withStatus}

class CaseStatusViewSpec extends ViewSpec {

  "Case Status" should {

    "render CANCELLED without the reason code" in {
      // When
      val c = aCase(
        withStatus(CANCELLED),
        withDecision(cancellation = None)
      )

      val doc = view(case_status(c, "id"))

      // Then
      doc.text() shouldBe "CANCELLED"
      doc.getElementById("id") should haveClass("bg-gray--dark")
    }

    "render CANCELLED and the reason code" in {
      // When
      val c = aCase(
        withStatus(CANCELLED),
        withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED)))
      )

      val doc = view(case_status(c, "id"))

      // Then
      doc.text() shouldBe "CANCELLED - 55"
      doc.getElementById("id") should haveClass("bg-gray--dark")
    }

    "render EXPIRED for expired rulings" in {
      // When
      val doc = view(case_status(Cases.btiCaseWithExpiredRuling, "id"))

      // Then
      doc.text() shouldBe "EXPIRED"
      doc.getElementById("id") should haveClass("bg-gray--dark")
    }

    "render the case status" in {
      // When
      val doc = view(case_status(Cases.btiCaseExample, "id"))

      // Then
      doc.text() shouldBe "OPEN"
      doc.getElementById("id") should haveClass("bg-blue")
    }

  }

}
