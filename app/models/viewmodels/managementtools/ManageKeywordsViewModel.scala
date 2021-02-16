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

package models.viewmodels.managementtools

import models._

case class ManageKeywordsTab(tabMessageKey: String, elementId: String, searchResult: Paged[Keyword])

case class ManageKeywordsViewModel(
  headingMessageKey: String,
  keywordsForApprovalTab: ManageKeywordsTab,
  allKeywordsTab: KeywordsTabViewModel
)

object ManageKeywordsViewModel {
  def forManagedTeams(): ManageKeywordsViewModel =
    ManageKeywordsViewModel(
      "Manage keywords",
      ManageKeywordsTab("keywordsApproval", "approval_tab", Paged(Keywords.allKeywords.filter(k => !k.isApproved))),
      KeywordsTabViewModel("allKeywords", "all_keywords", Set("approved_keywords"), Keywords.allKeywords.toSeq)
    )
}

case class KeywordsTabViewModel(
  tabMessageKey: String,
  elementId: String,
  keyword: Set[String],
  globalKeywords: Seq[Keyword]
)
