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

package views.managementtools

import models.forms.KeywordForm
import models.viewmodels.managementtools._
import models.{ApplicationType, CaseStatus, Keyword, Paged}
import play.twirl.api.HtmlFormat
import views.ViewMatchers.containText
import views.ViewSpec
import views.html.managementtools.keywords_tab

class KeywordsTabSpec extends ViewSpec {

  val keyword =
    KeywordViewModel(
      "reference",
      "FIDGET SPINNER",
      "Alex Smith",
      "Space grade aluminium spinner",
      ApplicationType.LIABILITY,
      CaseStatusKeywordViewModel(CaseStatus.REFERRED, false),
      true
    )

  val emptyMangageKeywordsTab = ManageKeywordsTab("keyword_approval", "keyword_tab", Paged(Seq.empty))
  val manageKeywordsTab       = ManageKeywordsTab("keyword_approval", "keyword_tab", Paged(Seq(keyword)))
  val keywordsTabViewModel =
    KeywordsTabViewModel("allKeywords", "all_keywords", Set("approved_keywords"), Seq("KEYWORD1"))

  val manageKeywordsViewModel = ManageKeywordsViewModel("Manage keywords", manageKeywordsTab, keywordsTabViewModel)
  val keywords                = Seq(Keyword("shoes", true), Keyword("hats", true), Keyword("shirts", true))
  val form                    = KeywordForm.formWithAuto(keywords.map(_.name))

  def manageKeywordsView(t: ManageKeywordsViewModel = manageKeywordsViewModel): HtmlFormat.Appendable =
    keywords_tab(t, form)

  "KeywordTab" should {

    "include a heading, button and tabs" in {
      val doc = view(
        manageKeywordsView()
      )
      doc.getElementById("common-cases-heading")      should containText("Manage keywords")
      doc.getElementById("create-new-keyword-button") should containText("Create new keyword")
      doc.getElementById("manage-keywords-tabs") should containText(
        messages("management.manage-keywords.approve-tab-title", 1)
      )
      doc.getElementById("manage-keywords-tabs") should containText("All keywords")
    }

    "show correct message when there are no keywords for approval" in {
      val doc = view(
        manageKeywordsView(manageKeywordsViewModel.copy(keywordsForApprovalTab = emptyMangageKeywordsTab))
      )
      doc.getElementById("keyword_tab") should containText("There are no keywords waiting for approval.")
    }

    "include an edit keyword button" in {
      val doc = view(
        manageKeywordsView()
      )
      doc.getElementById("keyword_search-edit-keyword-button") should containText("Edit keyword")
    }
  }
}
