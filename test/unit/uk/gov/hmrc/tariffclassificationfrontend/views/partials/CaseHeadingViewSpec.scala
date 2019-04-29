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

import play.api.test.FakeRequest
import uk.gov.hmrc.tariffclassificationfrontend.controllers.SessionKeys._
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers.{containElementWithID, _}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.case_heading
import uk.gov.tariffclassificationfrontend.utils.Cases._

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
      doc should containElementWithID("case-reference")
      doc.getElementById("case-reference") should containText("Case ref")
      doc should containElementWithID("case-status")
      doc.getElementById("case-status") should containText("OPEN")
      doc shouldNot containElementWithID("back-link")
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
      doc shouldNot containElementWithID("review-status")
    }

    "Render with 'Appeal Status'" in {
      // Given
      val c = aCase(
        withDecision(appeal = Some(Appeal(AppealStatus.ALLOWED)))
      )

      // When
      val doc = view(case_heading(c))

      // Then
      doc should containElementWithID("appeal-status")
      doc.getElementById("appeal-status") should containText("Appeal allowed")
    }

    "Render with 'Review Status'" in {
      // Given
      val c = aCase(
        withDecision(review = Some(Review(ReviewStatus.UPHELD)))
      )

      // When
      val doc = view(case_heading(c))

      // Then
      doc should containElementWithID("review-status")
      doc.getElementById("review-status") should containText("Review upheld")
    }

    "Render with queues back link" in {
      // Given
      val c = aCase()
      val requestWithSessionData = FakeRequest().withSession((backToQueuesLinkUrl, "url"), (backToQueuesLinkLabel, "some cases"))

      // When
      val doc = view(case_heading(c, displayBackLink = true)(messages, appConfig, requestWithSessionData))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should containText("some cases")
    }

    "Render with search results back link" in {
      // Given
      val c = aCase()
      val requestWithSessionData =
        FakeRequest().withSession((backToSearchResultsLinkUrl, "url"), (backToSearchResultsLinkLabel, "some search results"))
                     .withSession((backToQueuesLinkUrl, "url"), (backToQueuesLinkLabel, "some cases"))

      // When
      val doc = view(case_heading(c, displayBackLink = true)(messages, appConfig, requestWithSessionData))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should containText("some search results")
    }

    "Render back link when session is empty" in {
      // Given
      val c = aCase()
      val requestWithoutSessionData = FakeRequest()

      // When
      val doc = view(case_heading(c, displayBackLink = true)(messages, appConfig, requestWithoutSessionData))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should containText("Back Home")
    }

  }

}
