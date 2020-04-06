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

import models.forms.RemoveAttachmentForm
import models.viewmodels.CaseHeaderViewModel
import models.{Case, StoredAttachment}
import play.twirl.api.HtmlFormat
import utils.Cases
import views.ViewSpec
import views.html.v2.remove_attachment

class RemoveAttachmentViewSpec extends ViewSpec {

  lazy val attachment: StoredAttachment = Cases.storedAttachment.copy()
  lazy val caseOne: Case = Cases.btiCaseExample.copy(reference = "ref")
  val testTabIndex = 99

  def renderWithoutError: HtmlFormat.Appendable = {
    val header = CaseHeaderViewModel.fromCase(caseOne)
    remove_attachment(header, RemoveAttachmentForm.form, "file test", "name test")
  }

  def remove_attachment: remove_attachment = app.injector.instanceOf[remove_attachment]

  "Remove attachment View" should {

    "render without errors" in {
      val doc = view(renderWithoutError)

      doc.getElementsByTag("error-summary").size() shouldBe 0
    }

    //    "render upload form" in {
    //      val doc = view(renderAttachmentsDetails)
    //
    //      doc.getElementsByTag("form").size() shouldBe 1
    //    }
    //
    //    "render upload form and correct heading" in {
    //      val doc = view(renderAttachmentsDetails)
    //
    //      doc.getElementById("attachments-heading").text() shouldBe messages("case.menu.attachments")
    //    }
    //
    //    "render upload form and empty attachments table" in {
    //      val doc = view(renderAttachmentsDetails)
    //
    //      doc.getElementById("all-empty-table").text() shouldBe messages("case.attachment.table.empty")
    //      doc.getElementsByTag("table").size() shouldBe 0
    //    }
    //
    //    "render upload form and some elements in attachments table" in {
    //      val doc = view(renderAttachmentsDetailsWithAttachments)
    //
    //      doc.getElementsByTag("table").size() shouldBe 1
    //    }
    //
    //    "render upload form and some elements in attachments table check edit details button" in {
    //      val doc = view(renderAttachmentsDetailsWithAttachments)
    //      val expectedMsg = messages("case.attachment.edit.file.text") + " " +
    //        messages("case.attachment.edit.file.hidden.text",attachment.fileName)
    //
    //      doc.getElementById(s"all-row-0-edit-attachment-details").text().trim shouldBe expectedMsg
    //    }
    //
    //    "render upload form and some elements in attachments table check remove attachment button" in {
    //      val doc = view(renderAttachmentsDetailsWithAttachments)
    //      val expectedMsg = messages("case.attachment.remove.file.text") + " " +
    //        messages("case.attachment.remove.file.hidden.text",attachment.fileName)
    //
    //      doc.getElementById(s"all-row-0-remove").text().trim shouldBe expectedMsg
    //    }
  }

}
