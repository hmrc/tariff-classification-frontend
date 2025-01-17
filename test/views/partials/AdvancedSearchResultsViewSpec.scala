/*
 * Copyright 2025 HM Revenue & Customs
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

import controllers.routes
import models._
import models.response.ScanStatus
import org.jsoup.nodes.Document
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.advanced_search_results

import java.time.{Instant, LocalDate, ZoneOffset}

class AdvancedSearchResultsViewSpec extends ViewSpec {

  private def instant(date: String): Instant = LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC)

  private def hasLink(id: String, text: String, attributes: Seq[(String, String)])(implicit doc: Document): Unit =
    s"render LINK with ID: $id, Text: $text, Attributes: $attributes" in {
      doc.getElementById(id).text() shouldBe text
      doc.getElementById(id)          should haveTag("a")
      attributes.map { case (key, value) => doc.getElementById(id) should haveAttribute(key, value) }
    }

  private def hasExpectedTextByIds(expectedContent: Seq[(String, String)])(implicit doc: Document) =
    expectedContent.map { case (id, expectedText) =>
      s"render ID: $id, ExpectedText: $expectedText" in {
        doc.getElementById(id).text() shouldBe expectedText
      }
    }

  private def haveElementIds(expectedElementIds: Seq[String])(implicit doc: Document) =
    expectedElementIds.map { id =>
      s"CONTAIN Element with Id: $id" in {
        doc should containElementWithID(id)
      }
    }

  private def notHaveElementsIds(expectedElementsNotBePresent: Seq[String])(implicit doc: Document) =
    expectedElementsNotBePresent.map { id =>
      s"NOT contain Element with ID: $id" in {
        doc shouldNot containElementWithID(id)
      }
    }

  "Advanced Search Results" when {

    "there are no search results" should {
      "render No Results" in {
        val doc = view(advanced_search_results(Paged.empty[SearchResult]))
        doc should containElementWithID("advanced_search_results-empty")
      }
    }

    "the view has optional fields as empty" should {

      val c =
        aCase(
          withReference("reference"),
          withStatus(CaseStatus.OPEN),
          withoutDecision(),
          withHolder(businessName = "business-name")
        )

      val searchResult = SearchResult(c, Seq.empty)

      implicit val doc: Document = view(advanced_search_results(results = Paged(Seq(searchResult))))

      val expectedElementIds =
        Seq(
          "advanced_search_results-row-0-reference",
          "advanced_search_results-row-0",
          "advanced_search_results-row-0-business_name",
          "advanced_search_results-row-0-status",
          "advanced_search_results-row-0-ruling_end",
          "advanced_search_results-row-0-decision_code"
        )

      val expectedContent: Seq[(String, String)] =
        Seq(
          "advanced_search_results-row-0-business_name" -> "business-name",
          "advanced_search_results-row-0-status"        -> "OPEN",
          "advanced_search_results-row-0-ruling_end"    -> "",
          "advanced_search_results-row-0-decision_code" -> ""
        )

      val expectedIdsNotBePresent =
        Seq(
          "advanced_search_results-empty",
          "advanced_search_results-row-0-appeal_status",
          "advanced_search_results-row-0-review_status",
          "advanced_search_results-row-0-attachments"
        )

      haveElementIds(expectedElementIds)
      notHaveElementsIds(expectedIdsNotBePresent)
      hasExpectedTextByIds(expectedContent)
      hasLink(
        "advanced_search_results-row-0-reference",
        "reference",
        Seq("href" -> routes.CaseController.get("reference").url)
      )
    }

    "render Results with optional fields present" should {

      val c =
        aCase(
          withReference("reference"),
          withStatus(CaseStatus.OPEN),
          withDecision(
            bindingCommodityCode = "commodity-code",
            effectiveStartDate = Some(instant("2019-01-01")),
            effectiveEndDate = Some(instant("2019-02-01")),
            appeal = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
          ),
          withHolder(businessName = "business-name")
        )

      val storedAttachment =
        StoredAttachment(
          "id",
          public = true,
          operator = None,
          url = Some("url"),
          fileName = Some("filename"),
          mimeType = Some("image/png"),
          scanStatus = Some(ScanStatus.READY),
          timestamp = Instant.now(),
          description = Some("test description"),
          shouldPublishToRulings = true
        )

      val searchResult = SearchResult(c, Seq(storedAttachment))

      implicit val doc: Document = view(advanced_search_results(Paged(Seq(searchResult))))

      val expectedElementIds =
        Seq(
          "advanced_search_results-row-0",
          "advanced_search_results-row-0-reference",
          "advanced_search_results-row-0-attachments",
          "advanced_search_results-row-0-attachments-0"
        )

      val expectedIdNotBePresent = Seq("advanced_search_results-empty")

      val expectedContent =
        Seq(
          "advanced_search_results-row-0-business_name" -> "business-name",
          "advanced_search_results-row-0-status"        -> "OPEN",
          "advanced_search_results-row-0-appeal_status" -> "UNDER APPEAL",
          "advanced_search_results-row-0-ruling_end"    -> "01 Feb 2019",
          "advanced_search_results-row-0-decision_code" -> "commodity-code"
        )

      haveElementIds(expectedElementIds)
      notHaveElementsIds(expectedIdNotBePresent)
      hasExpectedTextByIds(expectedContent)
      hasLink(
        "advanced_search_results-row-0-reference",
        "reference",
        Seq("href" -> routes.CaseController.get("reference").url)
      )

      val attachmentsLinkAttributes = Seq("href" -> "url", "target" -> "_blank")
      hasLink("advanced_search_results-row-0-attachments-0-link", "", attachmentsLinkAttributes)

      "have attachment with correct tag and attributes" in {
        doc.getElementById("advanced_search_results-row-0-attachments-0") should haveTag("img")
        doc.getElementById("advanced_search_results-row-0-attachments-0") should haveAttribute("src", "url")
        doc.getElementById("advanced_search_results-row-0-attachments-0") should haveAttribute(
          "alt",
          "Image filename for case reference"
        )
        doc.getElementById("advanced_search_results-row-0-attachments-0") should haveAttribute("title", "filename")
      }
    }
  }

  "NOT render non image types" in {

    val c = aCase(
      withReference("reference"),
      withStatus(CaseStatus.OPEN),
      withDecision(
        bindingCommodityCode = "commodity-code",
        effectiveStartDate = Some(instant("2019-01-01")),
        effectiveEndDate = Some(instant("2019-02-01")),
        appeal = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
      ),
      withHolder(businessName = "business-name")
    )

    val storedAttachment = StoredAttachment(
      "id",
      public = true,
      operator = None,
      url = Some("url"),
      fileName = Some("filename"),
      mimeType = Some("text/plain"),
      scanStatus = Some(ScanStatus.READY),
      timestamp = Instant.now(),
      description = Some("test description"),
      shouldPublishToRulings = true
    )

    val searchResult = SearchResult(c, Seq(storedAttachment))

    val doc = view(advanced_search_results(Paged(Seq(searchResult))))

    doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")
  }

  "NOT render images without URL" in {

    val c =
      aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode = "commodity-code",
          effectiveStartDate = Some(instant("2019-01-01")),
          effectiveEndDate = Some(instant("2019-02-01")),
          appeal = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
        ),
        withHolder(businessName = "business-name")
      )

    val storedAttachment =
      StoredAttachment(
        "id",
        public = true,
        operator = None,
        url = None,
        fileName = Some("filename"),
        mimeType = Some("image/png"),
        scanStatus = Some(ScanStatus.READY),
        timestamp = Instant.now(),
        description = Some("test description"),
        shouldPublishToRulings = true
      )

    val searchResult = SearchResult(c, Seq(storedAttachment))
    val doc          = view(advanced_search_results(Paged(Seq(searchResult))))
    doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")
  }

  "NOT render un-scanned images" in {

    val c =
      aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode = "commodity-code",
          effectiveStartDate = Some(instant("2019-01-01")),
          effectiveEndDate = Some(instant("2019-02-01")),
          appeal = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
        ),
        withHolder(businessName = "business-name")
      )

    val storedAttachment =
      StoredAttachment(
        "id",
        public = true,
        operator = None,
        url = Some("url"),
        fileName = Some("filename"),
        mimeType = Some("text/plain"),
        scanStatus = None,
        timestamp = Instant.now(),
        description = Some("test description"),
        shouldPublishToRulings = true
      )

    val searchResult = SearchResult(c, Seq(storedAttachment))
    val doc          = view(advanced_search_results(Paged(Seq(searchResult))))
    doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")
  }

  "NOT render quarantined images" in {

    val c =
      aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode = "commodity-code",
          effectiveStartDate = Some(instant("2019-01-01")),
          effectiveEndDate = Some(instant("2019-02-01")),
          appeal = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))
        ),
        withHolder(businessName = "business-name")
      )

    val storedAttachment =
      StoredAttachment(
        "id",
        public = true,
        operator = None,
        url = Some("url"),
        fileName = Some("filename"),
        mimeType = Some("text/plain"),
        scanStatus = Some(ScanStatus.FAILED),
        timestamp = Instant.now(),
        description = Some("test description"),
        shouldPublishToRulings = true
      )

    val searchResult = SearchResult(c, Seq(storedAttachment))
    val doc          = view(advanced_search_results(Paged(Seq(searchResult))))
    doc shouldNot containElementWithID("advanced_search_results-row-0-attachments")
  }
}
