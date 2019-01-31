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

import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.advanced_search_results
import uk.gov.tariffclassificationfrontend.utils.Cases._

class AdvancedSearchResultsViewSpec extends ViewSpec {

  "Advanced Search Results" should {

    "Render No Results" in {
      // When
      val doc = view(advanced_search_results(Seq.empty))

      // Then
      doc should containElementWithID("advanced_search_results-empty")
    }

    "Render Results with optional fields empty" in {
      // Given
      val c = aCase(
        withoutDecision(),
        withHolder(businessName = "business-name"),
        withBTIDetails(goodDescription = "good-description")
      )

      // When
      val doc = view(advanced_search_results(Seq(c)))

      // Then
      doc shouldNot containElementWithID("advanced_search_results-empty")
      doc should containElementWithID("advanced_search_results-row-0")
      doc should containElementWithID("advanced_search_results-row-0-business_name")
      doc.getElementById("advanced_search_results-row-0-business_name") should containText("business-name")
      doc should containElementWithID("advanced_search_results-row-0-description")
      doc.getElementById("advanced_search_results-row-0-description") should containText("good-description")
      doc should containElementWithID("advanced_search_results-row-0-decision_code")
      doc.getElementById("advanced_search_results-row-0-decision_code") should containText("N/A")
    }

    "Render Results with optional fields present" in {
      // Given
      val c = aCase(
        withDecision(bindingCommodityCode = "commodity-code"),
        withHolder(businessName = "business-name"),
        withBTIDetails(goodDescription = "good-description")
      )

      // When
      val doc = view(advanced_search_results(Seq(c)))

      // Then
      doc shouldNot containElementWithID("advanced_search_results-empty")
      doc should containElementWithID("advanced_search_results-row-0")
      doc should containElementWithID("advanced_search_results-row-0-business_name")
      doc.getElementById("advanced_search_results-row-0-business_name") should containText("business-name")
      doc should containElementWithID("advanced_search_results-row-0-description")
      doc.getElementById("advanced_search_results-row-0-description") should containText("good-description")
      doc should containElementWithID("advanced_search_results-row-0-decision_code")
      doc.getElementById("advanced_search_results-row-0-decision_code") should containText("commodity-code")
    }

    "Trim long goods description" in {
      // Given
      val c = aCase(
        withBTIDetails(goodDescription = "x" * 100)
      )

      // When
      val doc = view(advanced_search_results(Seq(c)))

      // Then
      doc should containElementWithID("advanced_search_results-row-0-description")
      doc.getElementById("advanced_search_results-row-0-description") should containText("x" * 50 + "...")
    }
  }

}
