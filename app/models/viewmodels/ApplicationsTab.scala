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

package models.viewmodels

import java.time.Instant

import models.viewmodels.CasesTabViewModel.btiApplicationExample
import models._

case class ApplicationsTab(tabMessageKey: String, applicationType: ApplicationType, elementId : String,  searchResult: Paged[Case])

case class ApplicationTabViewModel(headingMessageKey: String, applicationTabs: List[ApplicationsTab])

object ApplicationsTab {

  def atar(searchResult : Paged[Case] = Paged.empty) =
    ApplicationsTab("applicationTab.assignedToMe.atar",ApplicationType.ATAR, "atar_tab", searchResult)
  def liability(searchResult : Paged[Case] = Paged.empty) =
    ApplicationsTab("applicationTab.assignedToMe.liability",ApplicationType.LIABILITY, "liability_tab", searchResult)
  def correspondence(searchResult : Paged[Case] = Paged.empty) =
    ApplicationsTab("applicationTab.assignedToMe.correspondence",ApplicationType.CORRESPONDENCE, "correspondence_tab", searchResult)
  def miscellaneous(searchResult : Paged[Case] = Paged.empty) =
    ApplicationsTab("applicationTab.assignedToMe.miscellaneous", ApplicationType.MISCELLANEOUS,"miscellaneous_tab", searchResult)

  val btiCaseExample: Case = Case(
    "1",
    CaseStatus.OPEN,
    Instant.now(),
    0,
    None,
    None,
    None,
    btiApplicationExample,
    None,
    Seq(),
    Set.empty,
    Sample(),
    Some(Instant.now()),
    Some(5)
  )

  def assignedToMeCases(cases: Seq[Case]): ApplicationTabViewModel = {

    val atars = cases.filter(_.application.isBTI)

    val liabilities = cases.filter(_.application.isLiabilityOrder)

    ApplicationTabViewModel(
      "applicationTab.assignedToMe",
      List(
        ApplicationsTab.atar(Paged(atars)),
        ApplicationsTab.liability(Paged(liabilities)),
        ApplicationsTab.correspondence(),
        ApplicationsTab.miscellaneous()
      )
    )
  }

  def referredByMe = ApplicationTabViewModel(
    "applicationTab.referredByMe",
    List(
      ApplicationsTab.atar(Paged(Seq(btiCaseExample))),
      ApplicationsTab.liability(),
      ApplicationsTab.correspondence(),
      ApplicationsTab.miscellaneous()
    )
  )

  def completedByMe = ApplicationTabViewModel(
    "applicationTab.completedByMe",
    List(
      ApplicationsTab.atar(Paged(Seq(btiCaseExample))),
      ApplicationsTab.liability(),
      ApplicationsTab.correspondence(),
      ApplicationsTab.miscellaneous()
    )
  )
}



