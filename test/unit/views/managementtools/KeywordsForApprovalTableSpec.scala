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

import models.viewmodels.managementtools.{CaseHeader, Keyword, ManageKeywordsTab}
import models.{ApplicationType, CaseStatus, Paged}
import play.twirl.api.HtmlFormat
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.managementtools.keywords_for_approval_table

class KeywordsForApprovalTableSpec extends ViewSpec {

  val keyword =
    CaseHeader("FIDGET SPINNER", "Alex Smith", "Space grade aluminium spinner", ApplicationType.LIABILITY, CaseStatus.REFERRED, true)

  val manageKeywordsTab = ManageKeywordsTab("keyword_approval", "keyword", Paged(Seq(keyword)))

  def manageKeywordsView(): HtmlFormat.Appendable = keywords_for_approval_table(manageKeywordsTab)

  "KeywordApprovalSpec" should {

    "render successfully with correct elements" in {
      val doc = view(
        manageKeywordsView()
      )
      doc should containElementWithID("keyword_approval-table")
      doc should containElementWithID("keyword_approval-details")
      doc.getElementById("keyword_approval-keyword") should containText("Keyword")
      doc.getElementById("keyword_approval-user-name") should containText("User")
      doc.getElementById("keyword_approval-goods-id") should containText("Goods")
      doc.getElementById("keyword_approval-type-id") should containText("Case type")
      doc.getElementById("keyword_approval-status-id") should containText("Case status")
    }

      for ((result, index) <- manageKeywordsTab.searchResult.results.zipWithIndex) {
        s"populate keywords table with correct data: ${index}" in {
          val doc = view(manageKeywordsView())
          doc should containElementWithID(s"keyword_approval-details-$index")
          doc.getElementById(s"keyword_approval-keyword-$index") should containText(result.keyword)
          doc.getElementById(s"keyword_approval-user-name-$index") should containText(result.name)
          doc.getElementById(s"keyword_approval-goods-id-$index") should containText(result.goods)
          doc.getElementById(s"keyword_approval-type-id-$index") should containText(result.caseType.prettyName)
          doc.getElementById(s"keyword_approval-status-id-$index") should containText(result.caseStatus.toString)
        }
      }
  }

}
