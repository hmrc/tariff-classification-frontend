/*
 * Copyright 2025 HM Revenue & Customs
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

import models.*

case class ManageKeywordsTab(tabMessageKey: String, elementId: String, searchResult: Paged[KeywordViewModel])

case class ManageKeywordsViewModel(
  headingMessageKey: String,
  keywordsForApprovalTab: ManageKeywordsTab,
  allKeywordsTab: KeywordsTabViewModel
)

object ManageKeywordsViewModel {
  def forManagedTeams(
       caseKeywords: Paged[CaseKeywordRow],
       allKeywords: Seq[Keyword]
     ): ManageKeywordsViewModel = {

    val approvedKeywords = allKeywords.filter(_.approved)
    val approvedKeywordNames: Set[String] = approvedKeywords.map(_.name).toSet

    val keywordViewModel = caseKeywords.results.view.map { row =>
      val overdue = (row.caseType, row.liabilityStatus) match {
        case ("LIABILITY_ORDER", Some("LIVE")) if row.daysElapsed >= 5 => true
        case (_, _) if row.daysElapsed >= 30                           => true
        case _                                                         => false
      }

      val isNotApproved = !approvedKeywordNames.contains(row.keyword)

      val caseStatusViewModel = CaseStatusKeywordViewModel(
        caseStatus = CaseStatus.withName(row.status),
        overdue = overdue
      )

      KeywordViewModel(
        reference = row.reference,
        keyword = row.keyword,
        name = row.user.getOrElse(""),
        goods = row.goods.getOrElse(""),
        caseType = ApplicationType.withName(row.caseType),
        status = caseStatusViewModel,
        approved = isNotApproved
      )
    }.toSeq

    ManageKeywordsViewModel(
      "Manage keywords",
      ManageKeywordsTab(
        "keywordsApproval",
        "approval_tab",
        Paged(
          results = keywordViewModel.filter(_.approved),
          pageIndex = caseKeywords.pageIndex,
          pageSize = caseKeywords.pageSize,
          resultCount = caseKeywords.resultCount
        )
      ),
      KeywordsTabViewModel(
        "allKeywords",
        "all_keywords",
        Set("approved_keywords"),
        approvedKeywords.map(_.name)
      )
    )
  }
}

case class KeywordsTabViewModel(
 tabMessageKey: String,
 elementId: String,
 keyword: Set[String],
 globalKeywords: Seq[String]
)