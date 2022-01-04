/*
 * Copyright 2022 HM Revenue & Customs
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

import models.CaseStatus
import models.CaseStatus.CaseStatus

sealed abstract class PrimaryNavigationTab(val id: String)

case object HomeTab extends PrimaryNavigationTab("primary_nav_home_tab")

case object MyCasesTab extends PrimaryNavigationTab("primary_nav_my_cases_tab")

case object OpenCasesTab extends PrimaryNavigationTab("primary_nav_open_cases_tab")

case object GatewayCasesTab extends PrimaryNavigationTab("primary_nav_gateway_cases_tab")

case object ManagerToolsTab extends PrimaryNavigationTab("primary_nav_manager_tools_tab")

case object NoTabSelected extends PrimaryNavigationTab("primary_nav_no_tab_selected")

case class PrimaryNavigationViewModel(selectedTab: PrimaryNavigationTab)
case object PrimaryNavigationViewModel {
  def getSelectedTabBasedOnAssigneeAndStatus(caseStatus: CaseStatus, ownCase: Boolean) =
    (caseStatus, ownCase) match {
      case (CaseStatus.NEW, _) => GatewayCasesTab
      case (_, true)           => MyCasesTab
      case (_, _)              => OpenCasesTab
    }
}
