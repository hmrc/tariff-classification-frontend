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

import models._
import utils.Cases._
import views.ViewMatchers.{containElementWithID, _}
import views.ViewSpec
import views.html.partials.case_heading

class CaseHeadingViewSpec extends ViewSpec {

  "Case Heading" should {

    "Render" in {
      // Given
      val c = aCase(
        withReference("ref"),
        withStatus(CaseStatus.OPEN)
      )

      // When
      val doc = view(case_heading(c))

      // Then
      doc                                  should containElementWithID("case-reference")
      doc.getElementById("case-reference") should containText("ATaR case ref")
      doc                                  should containElementWithID("case-status")
      doc.getElementById("case-status")    should containText("OPEN")
    }

    "Render without Optional Statuses" in {
      // Given
      val c = aCase(
        withoutDecision()
      )

      // When
      val doc = view(case_heading(c))

      // Then
      doc shouldNot containElementWithID("appeal-status")
    }

    "Render with 'Appeal Status'" in {
      // Given
      val c = aCase(
        withDecision(appeal = Seq(Appeal("id", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1)))
      )

      // When
      val doc = view(case_heading(c))

      // Then
      doc                                 should containElementWithID("appeal-status")
      doc.getElementById("appeal-status") should containText("Appeal allowed")
    }

  }

}
