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

import java.time.{ZoneOffset, ZonedDateTime}

import uk.gov.hmrc.tariffclassificationfrontend.models.Operator
import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.attachments_list
import uk.gov.tariffclassificationfrontend.utils.Cases

class AttachmentsListViewSpec extends ViewSpec {

  "Attachments List" should {

    "Render Nothing given no attachments" in {
      // When
      val doc = view(attachments_list("MODULE", Seq.empty))

      // Then
      doc shouldNot containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-empty-table")
    }

    "Render attachments" in {
      val attachment = Cases.storedAttachment.copy(
        id = "FILE_ID",
        fileName = "name",
        url = Some("url"),
        scanStatus = Some(ScanStatus.READY),
        timestamp = ZonedDateTime.of(2019,1,1,0,0,0,0,ZoneOffset.UTC)
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment)))

      // Then
      doc should containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-row-0")
      doc should containElementWithID("MODULE-row-0-title")
      doc should containElementWithID("MODULE-row-0-date")
      doc.getElementById("MODULE-row-0-title") should containText("name")
      doc.getElementById("MODULE-row-0-date") should containText("01 Jan 2019")
    }

    "Hide 'uploaded by'" in {
      val attachment = Cases.storedAttachment.copy(id = "FILE_ID", fileName = "name", url = Some("url"), scanStatus = Some(ScanStatus.READY))

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment)))

      // Then
      doc shouldNot containElementWithID("MODULE-header-uploaded_by")
      doc shouldNot containElementWithID("MODULE-row-FILE_ID-uploaded_by")
    }

    "Render 'uploaded by'" in {
      val attachment = Cases.storedAttachment.copy(
        id = "FILE_ID",
        fileName = "name",
        operator = Some(Operator("id", Some("operator name")))
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), showUploadedBy = true))

      // Then
      doc should containElementWithID("MODULE-header-uploaded_by")
      doc should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("operator name")
    }

    "Render 'uploaded by' with unknown operator" in {
      val attachment = Cases.storedAttachment.copy(
        id = "FILE_ID",
        fileName = "name",
        operator = None
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), showUploadedBy = true))

      // Then
      doc should containElementWithID("MODULE-header-uploaded_by")
      doc should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("Unknown")
    }

    "Render 'uploaded by' with unknown operator name" in {
      val attachment = Cases.storedAttachment.copy(
        id = "FILE_ID",
        fileName = "name",
        operator = Some(Operator("id", None))
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), showUploadedBy = true))

      // Then
      doc should containElementWithID("MODULE-header-uploaded_by")
      doc should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("Unknown")
    }

  }

}
