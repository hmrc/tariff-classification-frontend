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

import uk.gov.hmrc.tariffclassificationfrontend.forms.{CommodityCodeConstraints, DecisionForm, DecisionFormData}
import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, StoredAttachment}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.ruling_details_edit
import uk.gov.tariffclassificationfrontend.utils.Cases._
import org.scalatest.mockito.MockitoSugar

class EditRulingDetailsViewSpec extends ViewSpec with MockitoSugar {

  private val decisionForm = new DecisionForm(mock[CommodityCodeConstraints])

  "Edit Ruling Details" should {

    "Render Optional Application Fields" in {
      // Given
      val c = aCase(
        withOptionalApplicationFields(envisagedCommodityCode = Some("envisaged code"))
      )

      // When
      val doc = view(ruling_details_edit(c, Seq.empty, decisionForm.form))

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
      val doc = view(ruling_details_edit(c, Seq.empty, decisionForm.form))

      // Then
      doc should containElementWithID("envisagedCommodityCodeValue")
    }

    "Render pre populated Decision details" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN)
      )
      val formData = DecisionFormData(
        bindingCommodityCode = "commodity code",
        justification = "justification",
        goodsDescription = "goods description",
        methodSearch = "method search",
        methodExclusion = "method exclusion",
        methodCommercialDenomination = "commercial denomination"
      )

      // When
      val doc = view(ruling_details_edit(c, Seq.empty, decisionForm.form.fill(formData)))

      // Then
      doc should containElementWithID("bindingCommodityCode")
      doc.getElementById("bindingCommodityCode") should haveAttribute("value", "commodity code")
      doc should containElementWithID("goodsDescription")
      doc.getElementById("goodsDescription") should containText("goods description")
      doc should containElementWithID("justification")
      doc.getElementById("justification") should containText("justification")
      doc should containElementWithID("methodSearch")
      doc.getElementById("methodSearch") should containText("method search")
      doc should containElementWithID("methodCommercialDenomination")
      doc.getElementById("methodCommercialDenomination") should containText("commercial denomination")
      doc should containElementWithID("methodExclusion")
      doc.getElementById("methodExclusion") should containText("method exclusion")
    }

    "Render safe attachments" in {
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
        scanStatus = Some(ScanStatus.READY),
        timestamp = Instant.now()
      )

      // When
      val doc = view(ruling_details_edit(c, Seq(stored), decisionForm.form))

      // Then
      doc shouldNot containElementWithID("edit-ruling-no_attachments")
      doc should containElementWithID("edit-ruling-file-0")
      doc should containElementWithID("attachments[]")
    }

    "Not render unsafe attachments" in {
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
        scanStatus = Some(ScanStatus.FAILED),
        timestamp = Instant.now()
      )

      // When
      val doc = view(ruling_details_edit(c, Seq(stored), decisionForm.form))

      // Then
      doc should containElementWithID("edit-ruling-no_attachments")
      doc shouldNot containElementWithID("edit-ruling-file-FILE_ID")
      doc shouldNot containElementWithID("attachments[]")
    }

    "Render attachments pre-selected when 'public'" in {
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
        scanStatus = Some(ScanStatus.READY),
        timestamp = Instant.now()
      )

      // When
      val doc = view(ruling_details_edit(c, Seq(stored), decisionForm.form))

      // Then
      doc should containElementWithID("attachments[]")
      doc.getElementById("attachments[]") should haveAttribute("checked", "checked")
    }

    "Render attachments not pre-selected when not 'public'" in {
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
        scanStatus = Some(ScanStatus.READY),
        timestamp = Instant.now()
      )

      // When
      val doc = view(ruling_details_edit(c, Seq(stored), decisionForm.form))

      // Then
      doc should containElementWithID("attachments[]")
      doc.getElementById("attachments[]") shouldNot haveAttribute("checked", "checked")
    }

    "Render as link when URL available" in {
      // Given
      val c = aCase(
        withDecision()
      )
      val stored = StoredAttachment(
        id = "FILE_ID",
        public = false,
        operator = None,
        fileName = "file.txt",
        url = Some("url"),
        mimeType = "text/plain",
        scanStatus = Some(ScanStatus.READY),
        timestamp = Instant.now()
      )

      // When
      val doc = view(ruling_details_edit(c, Seq(stored), decisionForm.form))

      // Then
      doc should containElementWithID("edit-ruling-file-0")
      doc.getElementById("edit-ruling-file-0") should haveTag("a")
      doc.getElementById("edit-ruling-file-0") should haveAttribute("href", "url")
      doc.getElementById("edit-ruling-file-0") should containText("file.txt")
    }

    "Render as text when URL not available" in {
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
        scanStatus = Some(ScanStatus.READY),
        timestamp = Instant.now()
      )

      // When
      val doc = view(ruling_details_edit(c, Seq(stored), decisionForm.form))

      // Then
      doc should containElementWithID("edit-ruling-file-0")
      doc.getElementById("edit-ruling-file-0") should haveTag("span")
      doc.getElementById("edit-ruling-file-0") should containText("file.txt")
    }
  }
}
