/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.{Instant, LocalDate, ZoneOffset}

import controllers.routes
import models._
import models.response.ScanStatus
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.advanced_search_results

class AdvancedSearchResultsViewSpec extends ViewSpec {

  "Advanced Search Results" should {

    "Render No Results" in {
      // When
      val doc = view(advanced_search_results(Paged.empty[SearchResult]))

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

      val searchResult = SearchResult(c, Seq.empty)

      // When
      val doc = view(advanced_search_results(Paged(Seq(searchResult))))

      // Then
      doc shouldNot containElementWithID("advanced_search_results-empty")
      doc                                                           should containElementWithID("advanced_search_results-row-0")
      doc                                                           should containElementWithID("advanced_search_results-row-0-reference")
      doc.getElementById("advanced_search_results-row-0-reference") should containText("reference")
      doc.getElementById("advanced_search_results-row-0-reference") should haveTag("a")
      doc.getElementById("advanced_search_results-row-0-reference") should haveAttribute(
        "href",
        routes.CaseController.get("reference").url
      )
      doc                                                               should containElementWithID("advanced_search_results-row-0-business_name")
      doc.getElementById("advanced_search_results-row-0-business_name") should containText("business-name")
      doc                                                               should containElementWithID("advanced_search_results-row-0-status")
      doc.getElementById("advanced_search_results-row-0-status")        should containText("OPEN")

      doc shouldNot containElementWithID("advanced_search_results-row-0-appeal_status")
      doc shouldNot containElementWithID("advanced_search_results-row-0-review_status")
      doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")

      doc                                                                    should containElementWithID("advanced_search_results-row-0-ruling_end")
      doc.getElementById("advanced_search_results-row-0-ruling_end").text    shouldBe ""
      doc                                                                    should containElementWithID("advanced_search_results-row-0-decision_code")
      doc.getElementById("advanced_search_results-row-0-decision_code").text shouldBe ""
    }

    "Render Results with optional fields present" in {
      // Given
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode = "commodity-code",
          effectiveStartDate   = Some(instant("2019-01-01")),
          effectiveEndDate     = Some(instant("2019-02-01")),
          appeal               = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
        ),
        withHolder(businessName = "business-name")
      )

      val storedAttachment = StoredAttachment(
        "id",
        public                 = true,
        operator               = None,
        url                    = Some("url"),
        fileName               = Some("filename"),
        mimeType               = Some("image/png"),
        scanStatus             = Some(ScanStatus.READY),
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = true
      )

      val searchResult = SearchResult(c, Seq(storedAttachment))

      // When
      val doc = view(advanced_search_results(Paged(Seq(searchResult))))

      // Then
      doc shouldNot containElementWithID("advanced_search_results-empty")
      doc                                                           should containElementWithID("advanced_search_results-row-0")
      doc                                                           should containElementWithID("advanced_search_results-row-0-reference")
      doc.getElementById("advanced_search_results-row-0-reference") should containText("reference")
      doc.getElementById("advanced_search_results-row-0-reference") should haveTag("a")
      doc.getElementById("advanced_search_results-row-0-reference") should haveAttribute(
        "href",
        routes.CaseController.get("reference").url
      )
      doc                                                               should containElementWithID("advanced_search_results-row-0-business_name")
      doc.getElementById("advanced_search_results-row-0-business_name") should containText("business-name")
      doc                                                               should containElementWithID("advanced_search_results-row-0-status")
      doc.getElementById("advanced_search_results-row-0-status")        should containText("OPEN")

      doc                                                               should containElementWithID("advanced_search_results-row-0-appeal_status")
      doc.getElementById("advanced_search_results-row-0-appeal_status") should containText("Under appeal")

      doc                                                               should containElementWithID("advanced_search_results-row-0-attachments")
      doc                                                               should containElementWithID("advanced_search_results-row-0-attachments-0")
      doc.getElementById("advanced_search_results-row-0-attachments-0") should haveTag("img")
      doc.getElementById("advanced_search_results-row-0-attachments-0") should haveAttribute("src", "url")
      doc.getElementById("advanced_search_results-row-0-attachments-0") should haveAttribute(
        "alt",
        "Image filename for case reference"
      )
      doc.getElementById("advanced_search_results-row-0-attachments-0") should haveAttribute("title", "filename")

      doc                                                                    should containElementWithID("advanced_search_results-row-0-attachments-0-link")
      doc.getElementById("advanced_search_results-row-0-attachments-0-link") should haveAttribute("href", "url")
      doc.getElementById("advanced_search_results-row-0-attachments-0-link") should haveAttribute("target", "_blank")

      doc                                                               should containElementWithID("advanced_search_results-row-0-ruling_end")
      doc.getElementById("advanced_search_results-row-0-ruling_end")    should containText("01 Feb 2019")
      doc                                                               should containElementWithID("advanced_search_results-row-0-decision_code")
      doc.getElementById("advanced_search_results-row-0-decision_code") should containText("commodity-code")
    }
  }

  "Not render non image types" in {
    // Given
    val c = aCase(
      withReference("reference"),
      withStatus(CaseStatus.OPEN),
      withDecision(
        bindingCommodityCode = "commodity-code",
        effectiveStartDate   = Some(instant("2019-01-01")),
        effectiveEndDate     = Some(instant("2019-02-01")),
        appeal               = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
      ),
      withHolder(businessName = "business-name")
    )

    val storedAttachment = StoredAttachment(
      "id",
      public                 = true,
      operator               = None,
      url                    = Some("url"),
      fileName               = Some("filename"),
      mimeType               = Some("text/plain"),
      scanStatus             = Some(ScanStatus.READY),
      timestamp              = Instant.now(),
      description            = Some("test description"),
      shouldPublishToRulings = true
    )

    val searchResult = SearchResult(c, Seq(storedAttachment))

    // When
    val doc = view(advanced_search_results(Paged(Seq(searchResult))))

    // Then
    doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")
  }

  "Not render images without URL" in {
    // Given
    val c = aCase(
      withReference("reference"),
      withStatus(CaseStatus.OPEN),
      withDecision(
        bindingCommodityCode = "commodity-code",
        effectiveStartDate   = Some(instant("2019-01-01")),
        effectiveEndDate     = Some(instant("2019-02-01")),
        appeal               = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
      ),
      withHolder(businessName = "business-name")
    )

    val storedAttachment = StoredAttachment(
      "id",
      public                 = true,
      operator               = None,
      url                    = None,
      fileName               = Some("filename"),
      mimeType               = Some("image/png"),
      scanStatus             = Some(ScanStatus.READY),
      timestamp              = Instant.now(),
      description            = Some("test description"),
      shouldPublishToRulings = true
    )

    val searchResult = SearchResult(c, Seq(storedAttachment))

    // When
    val doc = view(advanced_search_results(Paged(Seq(searchResult))))

    // Then
    doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")
  }

  "Not render un-scanned images" in {
    // Given
    val c = aCase(
      withReference("reference"),
      withStatus(CaseStatus.OPEN),
      withDecision(
        bindingCommodityCode = "commodity-code",
        effectiveStartDate   = Some(instant("2019-01-01")),
        effectiveEndDate     = Some(instant("2019-02-01")),
        appeal               = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
      ),
      withHolder(businessName = "business-name")
    )

    val storedAttachment = StoredAttachment(
      "id",
      public                 = true,
      operator               = None,
      url                    = Some("url"),
      fileName               = Some("filename"),
      mimeType               = Some("text/plain"),
      scanStatus             = None,
      timestamp              = Instant.now(),
      description            = Some("test description"),
      shouldPublishToRulings = true
    )

    val searchResult = SearchResult(c, Seq(storedAttachment))

    // When
    val doc = view(advanced_search_results(Paged(Seq(searchResult))))

    // Then
    doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")
  }

  "Not render quarantined images" in {
    // Given
    val c = aCase(
      withReference("reference"),
      withStatus(CaseStatus.OPEN),
      withDecision(
        bindingCommodityCode = "commodity-code",
        effectiveStartDate   = Some(instant("2019-01-01")),
        effectiveEndDate     = Some(instant("2019-02-01")),
        appeal               = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
      ),
      withHolder(businessName = "business-name")
    )

    val storedAttachment = StoredAttachment(
      "id",
      public                 = true,
      operator               = None,
      url                    = Some("url"),
      fileName               = Some("filename"),
      mimeType               = Some("text/plain"),
      scanStatus             = Some(ScanStatus.FAILED),
      timestamp              = Instant.now(),
      description            = Some("test description"),
      shouldPublishToRulings = true
    )

    val searchResult = SearchResult(c, Seq(storedAttachment))

    // When
    val doc = view(advanced_search_results(Paged(Seq(searchResult))))

    // Then
    doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")
  }

  def instant(date: String): Instant = LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC)

}
