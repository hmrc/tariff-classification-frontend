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

package views.v2

import models.forms.RemoveAttachmentForm
import models.viewmodels.CaseHeaderViewModel
import models.{Case, StoredAttachment}
import play.api.data.FormError
import play.twirl.api.HtmlFormat
import utils.Cases
import views.ViewSpec
import views.html.v2.remove_attachment

class RemoveAttachmentViewSpec extends ViewSpec {

  lazy val attachment: StoredAttachment = Cases.storedAttachment.copy()
  lazy val caseOne: Case                = Cases.btiCaseExample.copy(reference = "ref")
  val testTabIndex                      = 99

  def renderWithoutError: HtmlFormat.Appendable = {
    val header = CaseHeaderViewModel.fromCase(caseOne)
    remove_attachment(header, RemoveAttachmentForm.form, "file test", "name test")
  }

  def renderWithError: HtmlFormat.Appendable = {
    val header = CaseHeaderViewModel.fromCase(caseOne)
    remove_attachment(
      header,
      RemoveAttachmentForm.form.copy(errors = Seq(FormError("remove_attachment", "Test error"))),
      "file test",
      "name test"
    )
  }

  def remove_attachment: remove_attachment = injector.instanceOf[remove_attachment]

  "Remove attachment View" should {

    "render without errors" in {
      val doc = view(renderWithoutError)

      doc.getElementsByTag("govuk-list govuk-error-summary__list").size() shouldBe 0
    }

    "render with errors" in {
      val doc = view(renderWithError)

      doc.getElementsByClass("govuk-list govuk-error-summary__list").size() shouldBe 1
    }

    "render without errors check question" in {
      val doc = view(renderWithoutError)

      doc
        .getElementsByClass("govuk-heading-xl")
        .text()
        .trim shouldBe "Are you sure you want to remove name test from this case?"
    }

    "render without errors check form" in {
      val doc                   = view(renderWithoutError)
      val expected: Seq[String] = Seq("Yes", "No", "Confirm")
      val actual: Seq[String]   = doc.getElementsByTag("form").text().split(" ").toSeq

      actual shouldBe expected
    }
  }

}
