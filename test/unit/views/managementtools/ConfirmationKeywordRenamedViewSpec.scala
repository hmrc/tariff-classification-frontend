/*
 * Copyright 2021 HM Revenue & Customs
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

package views.managementtools

import play.twirl.api.HtmlFormat
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.confirmation_keyword_renamed

class ConfirmationKeywordRenamedViewSpec extends ViewSpec {

  val oldKeyword = "APPLE"
  val newKeyword = "APPLES"

  def confirmKeywordRenamedView: confirmation_keyword_renamed = injector.instanceOf[confirmation_keyword_renamed]

  "ConfirmationKeywordRenamedView" should {

    "render successfully with the default tab" in {

      val doc = view(
        confirmKeywordRenamedView(
          oldKeyword,
          newKeyword
        )
      )
      doc                                              should containText(messages("management.manage-keywords.edit-approved-keywords.renamed.heading"))
      doc                                              should containElementWithID("confirm_keyword_renamed_id")
      doc.getElementById("confirm_keyword_renamed_id") should containText(oldKeyword.toUpperCase)
      doc.getElementById("confirm_keyword_renamed_id") should containText(newKeyword.toUpperCase)
      doc should containText(
        messages("management.manage-keywords.edit-approved-keywords.renamed.p", newKeyword)
      )
    }

    "contain a link to redirect to manage all keywords page" in {

      val doc = view(
        confirmKeywordRenamedView(
          oldKeyword,
          newKeyword
        )
      )

      doc should containText(messages("management.create-keyword.new-keyword-done.next"))
      doc.getElementById("manage-all-keywords-link") should containText(
        messages("management.create-keyword.new-keyword-done.link")
      )
    }
  }

}
