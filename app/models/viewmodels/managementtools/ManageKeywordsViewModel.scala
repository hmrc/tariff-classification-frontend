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

package models.viewmodels.managementtools

import models._

case class ManageKeywordsTab(tabMessageKey: String, elementId: String, searchResult: Paged[KeywordViewModel])

case class ManageKeywordsViewModel(
  headingMessageKey: String,
  keywordsForApprovalTab: ManageKeywordsTab,
  allKeywordsTab: KeywordsTabViewModel
)

object ManageKeywordsViewModel {
  def forManagedTeams(caseKeywords: Seq[CaseKeyword], allKeywords: Seq[Keyword]): ManageKeywordsViewModel = {

    val approvedKeywords = allKeywords.filter(_.approved)

    val keywordViewModel = caseKeywords.flatMap(caseKeyword =>
      caseKeyword.cases.map { caseHeader =>
        val overdue = (caseHeader.caseType, caseHeader.liabilityStatus) match {
          case (ApplicationType.LIABILITY, Some(LiabilityStatus.LIVE)) if caseHeader.daysElapsed >= 5 => true
          case (_, _) if caseHeader.daysElapsed >= 30                                                 => true
          case _                                                                                      => false
        }

        val caseStatus = CaseStatusKeywordViewModel(caseHeader.status, overdue)

        val notApprovedRejected = !allKeywords.exists(kw => caseKeyword.keyword.name == kw.name)

        KeywordViewModel(
          caseHeader.reference,
          caseKeyword.keyword.name,
          caseHeader.assignee
            .map { assignee =>
              assignee.name match {
                case Some(s) if !s.trim.isEmpty => s
                case _                          => assignee.id
              }
            }
            .getOrElse(""),
          caseHeader.goodsName.getOrElse(""),
          caseHeader.caseType,
          caseStatus,
          notApprovedRejected
        )
      }
    )

    ManageKeywordsViewModel(
      "Manage keywords",
      ManageKeywordsTab("keywordsApproval", "approval_tab", Paged(keywordViewModel.filter(_.isApproved))),
      KeywordsTabViewModel("allKeywords", "all_keywords", Set("approved_keywords"), approvedKeywords.map(_.name))
    )
  }

}

case class KeywordsTabViewModel(
  tabMessageKey: String,
  elementId: String,
  keyword: Set[String],
  globalKeywords: Seq[String]
)
