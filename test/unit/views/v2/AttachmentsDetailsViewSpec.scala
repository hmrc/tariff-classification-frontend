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
import models.viewmodels.{AttachmentsTabViewModel, LiabilityViewModel}
import play.twirl.api.HtmlFormat
import utils.Cases
import utils.Cases.{aCase, withLiabilityApplication, withReference}
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.v2.liability_view
import views.html.v2.partials.attachments_details

class AttachmentsDetailsViewSpec extends ViewSpec {

  val testTabIndex = 99

  def notRenderAttachmentsDetails: HtmlFormat.Appendable = {
    attachments_details(
      UploadAttachmentForm.form,
      AttachmentsTabViewModel("ref", Seq(), None),
      testTabIndex,
      showUploadAttachments = false
    )
  }

  def renderAttachmentsDetails: HtmlFormat.Appendable = {
    attachments_details(
      UploadAttachmentForm.form,
      AttachmentsTabViewModel("ref", Seq(), None),
      testTabIndex,
      showUploadAttachments = true
    )
  }

  def renderAttachmentsDetailsWithAttachments: HtmlFormat.Appendable = {
    attachments_details(
      UploadAttachmentForm.form,
      AttachmentsTabViewModel("ref", Seq(Cases.storedAttachment), None),
      testTabIndex,
      showUploadAttachments = true
    )
  }

  def attachments_details: attachments_details = app.injector.instanceOf[attachments_details]

  "Attachments Details View" should {

    "not render upload form" in {
      val doc = view(notRenderAttachmentsDetails)

      doc.getElementsByTag("form").size() shouldBe 0
    }

    "render upload form" in {
      val doc = view(renderAttachmentsDetails)

      doc.getElementsByTag("form").size() shouldBe 1
    }

    "render upload form and correct heading" in {
      val doc = view(renderAttachmentsDetails)

      doc.getElementById("attachments-heading").text() shouldBe messages("case.menu.attachments")
    }

    "render upload form and empty attachments table" in {
      val doc = view(renderAttachmentsDetails)

      doc.getElementById("all-empty-table").text() shouldBe messages("case.attachment.table.empty")
      doc.getElementsByTag("table").size() shouldBe 0
    }

    "render upload form and some elements in attachments table" in {
      val doc = view(renderAttachmentsDetailsWithAttachments)

      doc.getElementsByTag("table").size() shouldBe 1
    }
  }

}
