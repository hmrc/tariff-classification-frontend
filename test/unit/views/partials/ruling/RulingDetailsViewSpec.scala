/*
 * Copyright 2020 HM Revenue & Customs
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

package views.partials.ruling

import models._
import play.twirl.api.Html
import utils.Cases._
import views.ViewMatchers._
import views.html.case_details
import views.html.partials.ruling.ruling_details
import views.{CaseDetailPage, ViewSpec}

import java.time.Instant

class RulingDetailsViewSpec extends ViewSpec {

  "Ruling Details" should {

    "Render Optional Application Fields" in {
      // Given
      val c = aCase(
        withOptionalApplicationFields(envisagedCommodityCode = Some("envisaged code"))
      )

      // When
      val doc = view(ruling_details(c, None, Seq.empty, None))

      // Then
      doc                                               should containElementWithID("envisagedCommodityCodeValue")
      doc.getElementById("envisagedCommodityCodeValue") should containText("envisaged code")
    }

    "Render Optional Application Fields when empty" in {
      // Given
      val c = aCase(
        withOptionalApplicationFields(envisagedCommodityCode = None)
      )

      // When
      val doc = view(ruling_details(c, None, Seq.empty, None))

      // Then
      doc shouldNot containElementWithID("envisagedCommodityCodeValue")
    }

    "Render 'Edit' button for EDIT_RULING users" in {
      // Given
      val c = aCase(withReference("ref"), withStatus(CaseStatus.OPEN))

      // When
      val doc = view(
        ruling_details(c, None, Seq.empty, None)(requestWithPermissions(Permission.EDIT_RULING), messages, appConfig)
      )

      // Then
      doc                               should containElementWithID("ruling_edit_details")
      doc                               should containElementWithID("ruling_edit")
      doc.getElementById("ruling_edit") should haveTag("a")

      val call = controllers.routes.RulingController.editRulingDetails("ref")
      doc.getElementById("ruling_edit") should haveAttribute("href", call.url)
    }

    "Not render 'Edit' button when not permitted" in {
      // Given
      val c = aCase(withReference("ref"), withStatus(CaseStatus.OPEN))

      // When
      val doc = view(ruling_details(c, None, Seq.empty, None)(operatorRequest, messages, appConfig))

      // Then
      doc shouldNot containElementWithID("ruling_edit_details")
      doc shouldNot containElementWithID("ruling_edit")
    }

    "Not render Decision details if not present" in {
      // Given
      val c = aCase(withoutDecision())

      // When
      val doc = view(ruling_details(c, None, Seq.empty, None))

      // Then
      doc shouldNot containElementWithID("ruling_bindingCommodityCode")
      doc shouldNot containElementWithID("ruling_itemDescription")
      doc shouldNot containElementWithID("ruling_justification")
      doc shouldNot containElementWithID("ruling_searches")
      doc shouldNot containElementWithID("ruling_methodCommercialDenomination")
      doc shouldNot containElementWithID("ruling_exclusions")
      doc shouldNot containElementWithID("complete-case-button")

      doc should containElementWithID("no-ruling-information")
    }

    "Render Expiring commodity code" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode = "commodity code"
        )
      )
      val commodityCode = CommodityCode("commodity code", Some(Instant.now.plusSeconds(60)))

      // When
      val doc = view(
        ruling_details(c, None, Seq.empty, Some(commodityCode))(
          requestWithPermissions(Permission.COMPLETE_CASE),
          messages,
          appConfig
        )
      )

      // Then
      doc shouldNot containElementWithID("ruling_bindingCommodityCodeValue_expired")
    }

    "Render Expired commodity code" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode = "commodity code"
        )
      )
      val commodityCode = CommodityCode("commodity code", Some(Instant.now.minusSeconds(60)))

      // When
      val doc = view(
        ruling_details(c, None, Seq.empty, Some(commodityCode))(
          requestWithPermissions(Permission.COMPLETE_CASE),
          messages,
          appConfig
        )
      )

      // Then
      doc shouldNot containElementWithID("ruling_bindingCommodityCodeValue_expiring")
    }

    "Render commodity code expiration section" when {
      "case is COMPLETED and commodity code has expiry" in {
        // Given
        val c = aCase(
          withStatus(CaseStatus.COMPLETED),
          withDecision(
            bindingCommodityCode = "commodity code"
          )
        )
        val commodityCode = CommodityCode("commodity code", Some(Instant.now.minusSeconds(60)))

        // When
        val doc = view(
          ruling_details(c, None, Seq.empty, Some(commodityCode))(
            requestWithPermissions(Permission.COMPLETE_CASE),
            messages,
            appConfig
          )
        )

        // Then
        doc should containElementWithID("ruling_commodity_code_expiry_section")
      }

      "case is CANCELLED and commodity code has expiry" in {
        // Given
        val c = aCase(
          withStatus(CaseStatus.CANCELLED),
          withDecision(
            bindingCommodityCode = "commodity code",
            cancellation         = Some(Cancellation(reason = CancelReason.INVALIDATED_OTHER))
          )
        )
        val commodityCode = CommodityCode("commodity code", Some(Instant.now.minusSeconds(60)))

        // When
        val doc = view(
          ruling_details(c, None, Seq.empty, Some(commodityCode))(
            requestWithPermissions(Permission.COMPLETE_CASE),
            messages,
            appConfig
          )
        )

        // Then
        doc should containElementWithID("ruling_commodity_code_expiry_section")
      }

      "case is CANCELLED without reason" in {
        // Given
        val c = aCase(
          withStatus(CaseStatus.CANCELLED),
          withDecision(
            bindingCommodityCode = "commodity code"
          )
        )
        val commodityCode = CommodityCode("commodity code", Some(Instant.now.minusSeconds(60)))

        // When
        val doc = view(
          ruling_details(c, None, Seq.empty, Some(commodityCode))(
            requestWithPermissions(Permission.COMPLETE_CASE),
            messages,
            appConfig
          )
        )

        // Then
        doc should containElementWithID("ruling_commodity_code_expiry_section")
      }
    }

    "Not render commodity code expiration section" when {
      "commodity code has no expiry" in {
        // Given
        val c = aCase(
          withStatus(CaseStatus.COMPLETED),
          withDecision(
            bindingCommodityCode = "commodity code"
          )
        )
        val commodityCode = CommodityCode("commodity code", None)

        // When
        val doc = view(
          ruling_details(c, None, Seq.empty, Some(commodityCode))(
            requestWithPermissions(Permission.COMPLETE_CASE),
            messages,
            appConfig
          )
        )

        // Then
        doc shouldNot containElementWithID("ruling_commodity_code_expiry_section")
      }
    }

    "Render Decision details with COMPLETE_CASE permission" in {
      // Given
      val caseDetailsView = app.injector.instanceOf[case_details]
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode         = "commodity code",
          justification                = "justification",
          goodsDescription             = "goods description",
          methodSearch                 = Some("method search"),
          methodExclusion              = Some("method exclusion"),
          methodCommercialDenomination = Some("commercial denomination")
        )
      )

      // When
      val doc = view(
        ruling_details(c, None, Seq.empty, None)(requestWithPermissions(Permission.COMPLETE_CASE), messages, appConfig)
      )
      val doc_case_details = view(
        caseDetailsView(c, CaseDetailPage.RULING, Html("html"), None)(
          requestWithPermissions(Permission.COMPLETE_CASE),
          messages,
          appConfig
        )
      )

      // Then
      doc                                                            should containElementWithID("ruling_bindingCommodityCodeValue")
      doc.getElementById("ruling_bindingCommodityCodeValue")         should containText("commodity code")
      doc                                                            should containElementWithID("ruling_itemDescriptionValue")
      doc.getElementById("ruling_itemDescriptionValue")              should containText("goods description")
      doc                                                            should containElementWithID("ruling_justificationValue")
      doc.getElementById("ruling_justificationValue")                should containText("justification")
      doc                                                            should containElementWithID("ruling_searchesValue")
      doc.getElementById("ruling_searchesValue")                     should containText("method search")
      doc                                                            should containElementWithID("ruling_methodCommercialDenominationValue")
      doc.getElementById("ruling_methodCommercialDenominationValue") should containText("commercial denomination")
      doc                                                            should containElementWithID("ruling_exclusionsValue")
      doc.getElementById("ruling_exclusionsValue")                   should containText("method exclusion")
      doc_case_details                                               should containElementWithID("change-case-status-button")
    }

    "Render Decision details without Complete button for READ_ONLY users" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withDecision(
          bindingCommodityCode         = "commodity code",
          justification                = "justification",
          goodsDescription             = "goods description",
          methodSearch                 = Some("method search"),
          methodExclusion              = Some("method exclusion"),
          methodCommercialDenomination = Some("commercial denomination")
        )
      )

      // When
      val doc = view(ruling_details(c, None, Seq.empty, None)(operatorRequest, messages, appConfig))

      // Then
      doc                                                            should containElementWithID("ruling_bindingCommodityCodeValue")
      doc.getElementById("ruling_bindingCommodityCodeValue")         should containText("commodity code")
      doc                                                            should containElementWithID("ruling_itemDescriptionValue")
      doc.getElementById("ruling_itemDescriptionValue")              should containText("goods description")
      doc                                                            should containElementWithID("ruling_justificationValue")
      doc.getElementById("ruling_justificationValue")                should containText("justification")
      doc                                                            should containElementWithID("ruling_searchesValue")
      doc.getElementById("ruling_searchesValue")                     should containText("method search")
      doc                                                            should containElementWithID("ruling_methodCommercialDenominationValue")
      doc.getElementById("ruling_methodCommercialDenominationValue") should containText("commercial denomination")
      doc                                                            should containElementWithID("ruling_exclusionsValue")
      doc.getElementById("ruling_exclusionsValue")                   should containText("method exclusion")
      doc shouldNot containElementWithID("complete-case-button")
    }

    "Render Cancel Ruling when user has CANCEL_CASE permission" in {
      // Given
      val c = aCase(withReference("ref"), withStatus(CaseStatus.COMPLETED), withDecision())

      // When
      val doc = view(
        ruling_details(c, None, Seq.empty, None)(requestWithPermissions(Permission.CANCEL_CASE), messages, appConfig)
      )

      // Then
      doc should containElementWithID("cancel-ruling-button")
    }

    "Not Render Cancel Ruling when user does not have CANCEL_CASE permission" in {
      // Given
      val c = aCase(withReference("ref"), withStatus(CaseStatus.COMPLETED), withDecision())

      // When
      val doc = view(ruling_details(c, None, Seq.empty, None)(operatorRequest, messages, appConfig))

      // Then
      doc shouldNot containElementWithID("cancel-ruling-button")
    }

    "Render 'public' attachments" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id                     = "FILE_ID",
        public                 = true,
        operator               = None,
        fileName               = "file.txt",
        url                    = None,
        mimeType               = "text/plain",
        scanStatus             = None,
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = true
      )

      // When
      val doc = view(ruling_details(c, None, Seq(stored), None))

      // Then
      doc should containElementWithID("attachments-row-0-file")
    }

    "Not render 'non public' attachments" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id                     = "FILE_ID",
        public                 = false,
        operator               = None,
        fileName               = "file.txt",
        url                    = None,
        mimeType               = "text/plain",
        scanStatus             = None,
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = false
      )

      // When
      val doc = view(ruling_details(c, None, Seq(stored), None))

      // Then
      doc shouldNot containElementWithID("attachments-file-FILE_ID")
    }

  }

}
