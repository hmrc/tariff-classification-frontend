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

import java.time.Instant

import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, StoredAttachment}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.ruling_details
import uk.gov.tariffclassificationfrontend.utils.Cases._

class RulingDetailsViewSpec extends ViewSpec {

  "Ruling Details" should {

    "Render Optional Application Fields" in {
      // Given
      val c = aCase(
        withOptionalApplicationFields(envisagedCommodityCode = Some("envisaged code"))
      )

      // When
      val doc = view(ruling_details(c, None, Seq.empty))

      // Then
      doc should containElementWithID("envisagedCommodityCodeValue")
      doc.getElementById("envisagedCommodityCodeValue") should containText("envisaged code")
    }

    "Render Optional Application Fields when empty" in {
      // Given
      val c = aCase(
        withOptionalApplicationFields(envisagedCommodityCode = None)
      )

      // When
      val doc = view(ruling_details(c, None, Seq.empty))

      // Then
      doc should containElementWithID("envisagedCommodityCodeValue")
    }

    "Render 'Edit' button for OPEN cases" in {
      // Given
      val c = aCase(withReference("ref"), withStatus(CaseStatus.OPEN))

      // When
      val doc = view(ruling_details(c, None, Seq.empty))

      // Then
      doc should containElementWithID("ruling_edit_details")
      doc should containElementWithID("ruling_edit")
      doc.getElementById("ruling_edit") should haveTag("a")

      val call = uk.gov.hmrc.tariffclassificationfrontend.controllers.routes.RulingController.editRulingDetails("ref")
      doc.getElementById("ruling_edit") should haveAttribute("href", call.url)
    }

    "Not render 'Edit' button for other statuses" in {
      // Given
      val c = aCase(withReference("ref"), withStatus(CaseStatus.NEW))

      // When
      val doc = view(ruling_details(c, None, Seq.empty))

      // Then
      doc shouldNot containElementWithID("ruling_edit_details")
      doc shouldNot containElementWithID("ruling_edit")
    }

    "Not render Decision details if not present" in {
      // Given
      val c = aCase(withoutDecision())

      // When
      val doc = view(ruling_details(c, None, Seq.empty))

      // Then
      doc shouldNot containElementWithID("ruling_bindingCommodityCode")
      doc shouldNot containElementWithID("ruling_sanitisedGoodDescription")
      doc shouldNot containElementWithID("ruling_justification")
      doc shouldNot containElementWithID("ruling_searches")
      doc shouldNot containElementWithID("ruling_methodCommercialDenomination")
      doc shouldNot containElementWithID("ruling_exclusions")
      doc shouldNot containElementWithID("complete-case-button")

      doc should containElementWithID("no-ruling-information")
    }

    "Render Decision details" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode = "commodity code",
          justification = "justification",
          goodsDescription = "goods description",
          methodSearch = Some("method search"),
          methodExclusion = Some("method exclusion"),
          methodCommercialDenomination = Some("commercial denomination")
        )
      )

      // When
      val doc = view(ruling_details(c, None, Seq.empty))

      // Then
      doc should containElementWithID("ruling_bindingCommodityCodeValue")
      doc.getElementById("ruling_bindingCommodityCodeValue") should containText("commodity code")
      doc should containElementWithID("ruling_sanitisedGoodDescriptionValue")
      doc.getElementById("ruling_sanitisedGoodDescriptionValue") should containText("goods description")
      doc should containElementWithID("ruling_justificationValue")
      doc.getElementById("ruling_justificationValue") should containText("justification")
      doc should containElementWithID("ruling_searchesValue")
      doc.getElementById("ruling_searchesValue") should containText("method search")
      doc should containElementWithID("ruling_methodCommercialDenominationValue")
      doc.getElementById("ruling_methodCommercialDenominationValue") should containText("commercial denomination")
      doc should containElementWithID("ruling_exclusionsValue")
      doc.getElementById("ruling_exclusionsValue") should containText("method exclusion")
      doc should containElementWithID("complete-case-button")
    }

    "Render 'public' attachments" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id = "FILE_ID",
        public = true,
        operator = None,
        fileName = "file.txt",
        url = None,
        mimeType = "text/plain",
        scanStatus = None,
        timestamp = Instant.now()
      )

      // When
      val doc = view(ruling_details(c, None, Seq(stored)))

      // Then
      doc should containElementWithID("attachments-file-FILE_ID")
    }

    "Not render 'non public' attachments" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id = "FILE_ID",
        public = false,
        operator = None,
        fileName = "file.txt",
        url = None,
        mimeType = "text/plain",
        scanStatus = None,
        timestamp = Instant.now()
      )

      // When
      val doc = view(ruling_details(c, None, Seq(stored)))

      // Then
      doc shouldNot containElementWithID("attachments-file-FILE_ID")
    }

  }

}
