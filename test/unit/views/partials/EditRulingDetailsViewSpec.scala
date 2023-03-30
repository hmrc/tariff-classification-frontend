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

import models.forms.{CommodityCodeConstraints, DecisionForm, DecisionFormData}
import models.response.ScanStatus
import models.{CaseStatus, StoredAttachment}
import org.scalatestplus.mockito.MockitoSugar
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.ruling_details_edit

import java.time.Instant

class EditRulingDetailsViewSpec extends ViewSpec with MockitoSugar {

  private val decisionForm                       = new DecisionForm(mock[CommodityCodeConstraints])
  val rulingDetailsEditView: ruling_details_edit = app.injector.instanceOf[ruling_details_edit]

  "Edit Ruling Details" should {

    "Render Optional Application Fields" in {
      // Given
      val c = aCase(
        withOptionalApplicationFields(envisagedCommodityCode = Some("envisaged code"))
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq.empty, decisionForm.btiForm()))

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
      val doc = view(rulingDetailsEditView(c, Seq.empty, decisionForm.btiForm()))

      // Then
      doc should containElementWithID("envisagedCommodityCodeValue")
    }

    "Render pre populated Decision details" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN)
      )
      val formData = DecisionFormData(
        bindingCommodityCode         = "commodity code",
        justification                = "justification",
        goodsDescription             = "goods description",
        methodSearch                 = "method search",
        methodExclusion              = "method exclusion",
        methodCommercialDenomination = "commercial denomination"
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq.empty, decisionForm.btiForm().fill(formData)))

      // Then
      doc                                                should containElementWithID("bindingCommodityCode")
      doc.getElementById("bindingCommodityCode")         should haveAttribute("value", "commodity code")
      doc                                                should containElementWithID("goodsDescription")
      doc.getElementById("goodsDescription")             should containText("goods description")
      doc                                                should containElementWithID("justification")
      doc.getElementById("justification")                should containText("justification")
      doc                                                should containElementWithID("methodSearch")
      doc.getElementById("methodSearch")                 should containText("method search")
      doc                                                should containElementWithID("methodCommercialDenomination")
      doc.getElementById("methodCommercialDenomination") should containText("commercial denomination")
      doc                                                should containElementWithID("methodExclusion")
      doc.getElementById("methodExclusion")              should containText("method exclusion")
    }

    "Render safe, non-confidential attachments" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id                     = "FILE_ID",
        public                 = true,
        operator               = None,
        fileName               = Some("file.txt"),
        url                    = None,
        mimeType               = Some("text/plain"),
        scanStatus             = Some(ScanStatus.READY),
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = false
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq(stored), decisionForm.btiForm()))

      // Then
      doc shouldNot containElementWithID("edit-ruling-no_attachments")
      doc should containElementWithID("attachments-row-0-file")
      doc should containElementWithID("attachments[0]")
    }

    "Not render unsafe attachments" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id                     = "FILE_ID",
        public                 = false,
        operator               = None,
        fileName               = Some("file.txt"),
        url                    = None,
        mimeType               = Some("text/plain"),
        scanStatus             = Some(ScanStatus.FAILED),
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = false
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq(stored), decisionForm.btiForm()))

      // Then
      doc should containElementWithID("edit-ruling-no_attachments")
      doc shouldNot containElementWithID("attachments-row-0-file-FILE_ID")
      doc shouldNot containElementWithID("attachments[0]")
    }

    "Render publish checkbox as checked when 'shouldPublishToRulings'" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id                     = "FILE_ID",
        public                 = true,
        operator               = None,
        fileName               = Some("file.txt"),
        url                    = None,
        mimeType               = Some("text/plain"),
        scanStatus             = Some(ScanStatus.READY),
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = true
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq(stored), decisionForm.btiForm()))

      // Then
      doc                                  should containElementWithID("attachments[0]")
      doc.getElementById("attachments[0]") should haveAttribute("checked", "checked")
    }

    "Render publish checkbox unchecked when not 'shouldPublishToRulings'" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id                     = "FILE_ID",
        public                 = true,
        operator               = None,
        fileName               = Some("file.txt"),
        url                    = None,
        mimeType               = Some("text/plain"),
        scanStatus             = Some(ScanStatus.READY),
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = false
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq(stored), decisionForm.btiForm()))

      // Then
      doc should containElementWithID("attachments[0]")
      doc.getElementById("attachments[0]") shouldNot haveAttribute("checked", "checked")
    }

    "Render as link when URL available" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id                     = "FILE_ID",
        public                 = false,
        operator               = None,
        fileName               = Some("file.txt"),
        url                    = Some("url"),
        mimeType               = Some("text/plain"),
        scanStatus             = Some(ScanStatus.READY),
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = true
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq(stored), decisionForm.btiForm()))

      // Then
      doc                                          should containElementWithID("attachments-row-0-file")
      doc.getElementById("attachments-row-0-file") should haveTag("a")
      doc.getElementById("attachments-row-0-file") should haveAttribute(
        "href",
        s"/manage-tariff-classifications/attachment/${c.reference}/FILE_ID"
      )
      doc.getElementById("attachments-row-0-file") should containText("file.txt")
    }

    "Render as text when URL not available" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id                     = "FILE_ID",
        public                 = false,
        operator               = None,
        fileName               = Some("file.txt"),
        url                    = None,
        mimeType               = Some("text/plain"),
        scanStatus             = Some(ScanStatus.READY),
        timestamp              = Instant.now(),
        description            = Some("test description"),
        shouldPublishToRulings = false
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq(stored), decisionForm.btiForm()))

      // Then
      doc                                          should containElementWithID("attachments-row-0-file")
      doc.getElementById("attachments-row-0-file") should haveTag("span")
      doc.getElementById("attachments-row-0-file") should containText("file.txt")
    }

    "Render with commodity code autocomplete disabled" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN)
      )
      val formData = DecisionFormData(
        bindingCommodityCode         = "commodity code",
        justification                = "justification",
        goodsDescription             = "goods description",
        methodSearch                 = "method search",
        methodExclusion              = "method exclusion",
        methodCommercialDenomination = "commercial denomination"
      )

      // When
      val doc = view(rulingDetailsEditView(c, Seq.empty, decisionForm.btiForm().fill(formData)))

      // Then
      doc should containElementWithID("bindingCommodityCode")
      val commodityCodeInputField = doc.getElementById("bindingCommodityCode")
      commodityCodeInputField should haveAttribute("value", "commodity code")
      commodityCodeInputField should haveAttribute("autocomplete", "off")
    }
  }
}
