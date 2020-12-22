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

package views.partials

import models.response.ScanStatus
import models.{Operator, Permission}
import utils.Cases
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.attachments_list_atar

import java.time.{ZoneOffset, ZonedDateTime}

class AttachmentsListAtarViewSpec extends ViewSpec {

  "Attachments List" should {

    "Render Nothing given no attachments" in {
      // When
      val doc = view(attachments_list_atar("MODULE", Seq.empty, c = Cases.btiCaseExample))

      // Then
      doc shouldNot containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-empty-table")
    }

    "Render attachments" in {
      val attachment = Cases.storedAttachment.copy(
        id         = "FILE_ID",
        fileName   = "name",
        url        = Some("url"),
        scanStatus = Some(ScanStatus.READY),
        timestamp  = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
      )

      // When
      val doc = view(attachments_list_atar("MODULE", Seq(attachment), c = Cases.btiCaseExample))

      // Then
      doc                                      should containElementWithID("MODULE-table")
      doc                                      should containElementWithID("MODULE-row-0")
      doc                                      should containElementWithID("MODULE-row-0-title")
      doc.getElementById("MODULE-row-0-title") should containText("name")
    }

    "Render 'Added by'" in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = "name",
        operator = Some(Operator("id", Some("operator name")))
      )

      // When
      val doc = view(attachments_list_atar("MODULE", Seq(attachment), c = Cases.btiCaseExample))

      // Then
      doc                                            should containElementWithID("MODULE-header-uploaded_by")
      doc                                            should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("operator name")
    }

    "Render 'Added by' with unknown operator and with the applicant name" in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = "fileName_attachment",
        operator = None
      )

      val attachment_trader = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = "fileName"
      )

      // When
      val doc = view(
        attachments_list_atar(
          "MODULE",
          Seq(attachment, attachment_trader),
          c = Cases.btiCaseExample
        )
      )

      // Then
      doc                                            should containElementWithID("MODULE-header-uploaded_by")
      doc                                            should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("")
      doc.getElementById("MODULE-row-1-uploaded_by") should containText("name")
    }

    "Render 'Added by' with unknown operator name" in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = "name",
        operator = Some(Operator("id", None))
      )

      // When
      val doc = view(attachments_list_atar("MODULE", Seq(attachment), c = Cases.btiCaseExample))

      // Then
      doc                                            should containElementWithID("MODULE-header-uploaded_by")
      doc                                            should containElementWithID("MODULE-row-0-uploaded_by")
      doc.getElementById("MODULE-row-0-uploaded_by") should containText("Unknown")

    }

    "Render show edit details link when user has permission " in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = "name",
        operator = Some(Operator("id", Some("operator name")))
      )

      // When
      val doc = view(
        attachments_list_atar("MODULE", Seq(attachment), c = Cases.btiCaseExample)(
          requestWithPermissions(Permission.EDIT_ATTACHMENT_DETAIL),
          messages
        )
      )
      //TODO: uncomment when the functunallity is done

//      // Then
//      doc                                                        should containElementWithID("MODULE-row-0-edit-attachment-details")
//      doc.getElementById("MODULE-row-0-edit-attachment-details") should containText("Edit details")
    }

    "Do not render show edit details link when user does not have permission" in {
      val attachment = Cases.storedAttachment.copy(
        id       = "FILE_ID",
        fileName = "name",
        operator = Some(Operator("id", Some("operator name")))
      )

      // When
      val doc = view(attachments_list_atar("MODULE", Seq(attachment), c = Cases.btiCaseExample))

      // Then
      doc shouldNot containElementWithID("MODULE-row-0-remove")
    }

    "Status should display PUBLISHED if the file is public" in {
      val attachment = Cases.storedAttachment
        .copy(id = "FILE_ID", public = true, fileName = "name", url = Some("url"), scanStatus = Some(ScanStatus.READY))

      // When
      val doc = view(attachments_list_atar("MODULE", Seq(attachment), c = Cases.btiCaseExample))

      // Then
      doc should containText(messages("case.attachment.upload.status-published"))
    }

    "Status should display CONFIDENTIAL if the file is not public" in {
      val attachment = Cases.storedAttachment
        .copy(id = "FILE_ID", public = false, fileName = "name", url = Some("url"), scanStatus = Some(ScanStatus.READY))

      // When
      val doc = view(attachments_list_atar("MODULE", Seq(attachment), c = Cases.btiCaseExample))

      // Then
      doc should containText(messages("case.attachment.upload.status-confidential"))

    }

    "Status should display UPLOAD FAILED if the file is a virus" in {
      val attachment = Cases.storedAttachment
        .copy(
          id         = "FILE_ID",
          public     = false,
          fileName   = "name",
          url        = Some("url"),
          scanStatus = Some(ScanStatus.FAILED)
        )

      // When
      val doc = view(attachments_list_atar("MODULE", Seq(attachment), c = Cases.btiCaseExample))

      // Then
      doc should containText(messages("case.attachment.upload.status-failed"))

    }

  }

}
