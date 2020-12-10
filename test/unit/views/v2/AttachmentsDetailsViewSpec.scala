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

package views.v2

import models.forms.UploadAttachmentForm
import models.viewmodels.AttachmentsTabViewModel
import models.{Permission, StoredAttachment}
import play.twirl.api.HtmlFormat
import utils.Cases
import views.ViewSpec
import views.html.partials.liabilities.attachments_details

class AttachmentsDetailsViewSpec extends ViewSpec {

  lazy val attachment: StoredAttachment = Cases.storedAttachment.copy()

  def notRenderAttachmentsDetails: HtmlFormat.Appendable =
    attachments_details(
      UploadAttachmentForm.form,
      AttachmentsTabViewModel("ref", Seq(), None),
      showUploadAttachments = false
    )

  def renderAttachmentsDetails: HtmlFormat.Appendable =
    attachments_details(
      UploadAttachmentForm.form,
      AttachmentsTabViewModel("ref", Seq(), None),
      showUploadAttachments = true
    )

  def attachments_details: attachments_details = injector.instanceOf[attachments_details]

  def renderAttachmentsDetailsWithAttachments: HtmlFormat.Appendable =
    attachments_details(
      UploadAttachmentForm.form,
      AttachmentsTabViewModel("ref", Seq(attachment), None),
      showUploadAttachments = true
    )(requestWithPermissions(Permission.EDIT_ATTACHMENT_DETAIL, Permission.REMOVE_ATTACHMENTS), messages, appConfig)

  "Attachments Details View" should {

    "render tab title" in {
      val doc = view(notRenderAttachmentsDetails)

      doc.getElementsByTag("h2").text() shouldBe messages("case.menu.attachments")
    }

    "render hint under tab title" in {
      val doc = view(notRenderAttachmentsDetails)

      doc.getElementsByClass("form-hint").text() shouldBe messages("case.attachment.hint.text.header")
    }

    "not render upload form" in {
      val doc = view(notRenderAttachmentsDetails)

      doc.getElementsByTag("form").size() shouldBe 0
    }

    "render upload form" in {
      val doc = view(renderAttachmentsDetails)

      doc.getElementsByTag("form").size() shouldBe 1
    }

    "render upload form and empty attachments table" in {
      val doc = view(renderAttachmentsDetails)

      doc.getElementById("all-empty-table").text() shouldBe messages("case.attachment.table.empty")
      doc.getElementsByTag("table").size()         shouldBe 0
    }

    "render upload form and some elements in attachments table" in {
      val doc = view(renderAttachmentsDetailsWithAttachments)

      doc.getElementsByTag("table").size() shouldBe 1
    }

    "render upload form and some elements in attachments table check edit details button" in {
      val doc = view(renderAttachmentsDetailsWithAttachments)
      val expectedMsg = messages("case.attachment.edit.file.text") + " " +
        messages("case.attachment.edit.file.hidden.text", attachment.fileName)

      doc.getElementById(s"all-row-0-edit-attachment-details").text().trim shouldBe expectedMsg
    }

    "render upload form and some elements in attachments table check remove attachment button" in {
      val doc = view(renderAttachmentsDetailsWithAttachments)
      val expectedMsg = messages("case.attachment.remove.file.text") + " " +
        messages("case.attachment.remove.file.hidden.text", attachment.fileName)

      doc.getElementById(s"all-row-0-remove").text().trim shouldBe expectedMsg
    }
  }

}
