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

import java.time.{Instant, LocalDate, ZoneOffset}

import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
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
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withoutDecision(),
        withHolder(businessName = "business-name")
      )

      // When
      val doc = view(advanced_search_results(Seq(c)))

      // Then
      doc shouldNot containElementWithID("advanced_search_results-empty")
      doc should containElementWithID("advanced_search_results-row-0")
      doc should containElementWithID("advanced_search_results-row-0-reference")
      doc.getElementById("advanced_search_results-row-0-reference") should containText("reference")
      doc.getElementById("advanced_search_results-row-0-reference") should haveTag("a")
      doc.getElementById("advanced_search_results-row-0-reference") should haveAttribute("href", routes.CaseController.trader("reference").url)
      doc should containElementWithID("advanced_search_results-row-0-business_name")
      doc.getElementById("advanced_search_results-row-0-business_name") should containText("business-name")
      doc should containElementWithID("advanced_search_results-row-0-status")
      doc.getElementById("advanced_search_results-row-0-status") should containText("OPEN")

      doc should containElementWithID("advanced_search_results-row-0-ruling_start")
      doc.getElementById("advanced_search_results-row-0-ruling_start").text shouldBe empty
      doc should containElementWithID("advanced_search_results-row-0-ruling_end")
      doc.getElementById("advanced_search_results-row-0-ruling_end").text shouldBe empty
      doc should containElementWithID("advanced_search_results-row-0-decision_code")
      doc.getElementById("advanced_search_results-row-0-decision_code").text shouldBe empty
    }

    "Render Results with optional fields present" in {
      // Given
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withDecision(bindingCommodityCode = "commodity-code", effectiveStartDate = Some(instant("2019-01-01")), effectiveEndDate = Some(instant("2019-02-01"))),
        withHolder(businessName = "business-name")
      )

      // When
      val doc = view(advanced_search_results(Seq(c)))

      // Then
      doc shouldNot containElementWithID("advanced_search_results-empty")
      doc should containElementWithID("advanced_search_results-row-0")
      doc should containElementWithID("advanced_search_results-row-0-reference")
      doc.getElementById("advanced_search_results-row-0-reference") should containText("reference")
      doc.getElementById("advanced_search_results-row-0-reference") should haveTag("a")
      doc.getElementById("advanced_search_results-row-0-reference") should haveAttribute("href", routes.CaseController.trader("reference").url)
      doc should containElementWithID("advanced_search_results-row-0-business_name")
      doc.getElementById("advanced_search_results-row-0-business_name") should containText("business-name")
      doc should containElementWithID("advanced_search_results-row-0-status")
      doc.getElementById("advanced_search_results-row-0-status") should containText("OPEN")

      doc should containElementWithID("advanced_search_results-row-0-ruling_start")
      doc.getElementById("advanced_search_results-row-0-ruling_start") should containText("01 Jan 2019")
      doc should containElementWithID("advanced_search_results-row-0-ruling_end")
      doc.getElementById("advanced_search_results-row-0-ruling_end") should containText("01 Feb 2019")
      doc should containElementWithID("advanced_search_results-row-0-decision_code")
      doc.getElementById("advanced_search_results-row-0-decision_code") should containText("commodity-code")
    }

    def instant(date: String): Instant = LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC)
  }

}
