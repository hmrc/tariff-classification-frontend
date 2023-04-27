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

package views.partials.statuses

import models.CaseStatus.CANCELLED
import models.{CancelReason, Cancellation}
import utils.Cases
import utils.Cases.{aCase, withDecision, withStatus}
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.statuses.case_status

class CaseStatusViewSpec extends ViewSpec {

  "Case Status" should {

    "render CANCELLED without the reason code" in {

      val c = aCase(
        withStatus(CANCELLED),
        withDecision(cancellation = None)
      )

      val doc = view(case_status(c, "id"))

      doc.text()               shouldBe "CANCELLED"
      doc.getElementById("id") should haveClass("govuk-tag--red")
    }

    "render CANCELLED and the reason code" in {

      val c = aCase(
        withStatus(CANCELLED),
        withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED)))
      )

      val doc = view(case_status(c, "id"))

      doc.text()               shouldBe "CANCELLED - 55"
      doc.getElementById("id") should haveClass("govuk-tag--red")
    }

    "render EXPIRED for expired rulings" in {

      val doc = view(case_status(Cases.btiCaseWithExpiredRuling, "id"))

      doc.text()               shouldBe "case status EXPIRED"
      doc.getElementById("id") should haveClass("govuk-tag--green")
    }

    "render the case status" in {

      val doc = view(case_status(Cases.btiCaseExample, "id"))

      doc.text()               shouldBe "case status OPEN"
      doc.getElementById("id") should haveClass("govuk-tag--blue")
    }

    "render the live liability case status" in {

      val doc = view(case_status(Cases.liabilityLiveCaseExample, "id"))

      doc.text()                                shouldBe "liability type LIVE case status OPEN"
      doc.getElementById("id-liability-status") should haveClass("govuk-tag--pink")
    }

  }

}
