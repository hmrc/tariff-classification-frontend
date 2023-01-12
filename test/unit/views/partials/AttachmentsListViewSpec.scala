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

import java.time.{ZoneOffset, ZonedDateTime}

import models.response.ScanStatus
import models.{Operator, Permission}
import utils.Cases
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.attachments_list

class AttachmentsListViewSpec extends ViewSpec {

  private val caseRef: String = "600000004"

  "Attachments List" should {

    "Render Nothing given no attachments" in {
      // When
      val doc = view(attachments_list("MODULE", Seq.empty, caseRef = caseRef))

      // Then
      doc shouldNot containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-empty-table")
    }

    "Render attachments" in {
      val attachment = Cases.storedAttachment.copy(
        id         = "FILE_ID",
        fileName   = Some("name"),
        url        = Some("url"),
        scanStatus = Some(ScanStatus.READY),
        timestamp  = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), caseRef = caseRef))

      // Then
      doc                                      should containElementWithID("MODULE-table")
      doc                                      should containElementWithID("MODULE-row-0")
      doc                                      should containElementWithID("MODULE-row-0-title")
      doc                                      should containElementWithID("MODULE-row-0-date")
      doc.getElementById("MODULE-row-0-title") should containText("name")
      doc.getElementById("MODULE-row-0-date")  should containText("01 Jan 2019")
    }

    "Hide 'uploaded by'" in {
      val attachment = Cases.storedAttachment
        .copy(id = "FILE_ID", fileName = Some("name"), url = Some("url"), scanStatus = Some(ScanStatus.READY))

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), caseRef = caseRef))

      // Then
      doc shouldNot containElementWithID("MODULE-header-uploaded_by")
      doc shouldNot containElementWithID("MODULE-row-FILE_ID-uploaded_by")
    }

    "Render 'uploaded by'" in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = Some("name"),
        operator = Some(Operator("id", Some("operator name")))
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), showUploadedBy = true, caseRef = caseRef))

      // Then
      doc                                            should containElementWithID("MODULE-header-uploaded_by")
      doc                                            should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("operator name")
    }

    "Render 'uploaded by' with unknown operator" in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = Some("name"),
        operator = None
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), showUploadedBy = true, caseRef = caseRef))

      // Then
      doc                                            should containElementWithID("MODULE-header-uploaded_by")
      doc                                            should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("Unknown")
    }

    "Render 'uploaded by' with unknown operator name" in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = Some("name"),
        operator = Some(Operator("id", None))
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), showUploadedBy = true, caseRef = caseRef))

      // Then
      doc                                            should containElementWithID("MODULE-header-uploaded_by")
      doc                                            should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("Unknown")
    }

    "Render show remove link when user has permission " in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = Some("name"),
        operator = Some(Operator("id", Some("operator name")))
      )

      // When
      val doc = view(
        attachments_list("MODULE", Seq(attachment), showRemoval = true, caseRef = caseRef)(
          requestWithPermissions(Permission.REMOVE_ATTACHMENTS),
          messages
        )
      )

      // Then
      doc                                       should containElementWithID("MODULE-row-0-remove")
      doc.getElementById("MODULE-row-0-remove") should containText("Remove")
    }

    "Do not render show remove link when user does not have permission " in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = Some("name"),
        operator = Some(Operator("id", Some("operator name")))
      )

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment), showRemoval = true, caseRef = caseRef))

      // Then
      doc shouldNot containElementWithID("MODULE-row-0-remove")
    }

  }

}
