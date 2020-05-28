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

import models.CaseStatus.CaseStatus
import models._

case class LiabilityViewModel(
                               caseHeaderViewModel: CaseHeaderViewModel,
                               hasPermissions: Boolean,
                               showChangeCaseStatus: Boolean,
                               showTakeOffReferral: Boolean,
                               showReopen: Boolean,
                               caseStatus: CaseStatus
                             ) {

  def isNewCase: Boolean = caseStatus == CaseStatus.NEW

  def showRulingAndKeywordsTabs: Boolean =
    caseStatus == CaseStatus.OPEN ||
      caseStatus == CaseStatus.REFERRED ||
      caseStatus == CaseStatus.REJECTED ||
      caseStatus == CaseStatus.SUSPENDED ||
      caseStatus == CaseStatus.COMPLETED


  def showActionThisCase: Boolean = isNewCase && hasPermissions

  def showAdvancedSearchButton: Boolean =
    caseStatus == CaseStatus.OPEN ||
      caseStatus == CaseStatus.REFERRED ||
      caseStatus == CaseStatus.SUSPENDED
}

object LiabilityViewModel {

  def fromCase(c: Case, operator: Operator): LiabilityViewModel = {

    def releaseOrSuppressPermissions: Boolean =
      operator.permissions.contains(Permission.RELEASE_CASE) || operator.permissions.contains(Permission.SUPPRESS_CASE)

    def completeCasePermission: Boolean =
      operator.permissions.contains(Permission.COMPLETE_CASE)

    def reopenCasePermission: Boolean =
      operator.permissions.contains(Permission.REOPEN_CASE)

    def changeCaseStatus: Boolean =
      c.status == CaseStatus.OPEN && completeCasePermission

    def takeOffReferral: Boolean =
      c.status == CaseStatus.REFERRED && reopenCasePermission

    def showReopenButton: Boolean = c.status == CaseStatus.SUSPENDED && reopenCasePermission

    LiabilityViewModel(
      CaseHeaderViewModel.fromCase(c),
      hasPermissions = releaseOrSuppressPermissions,
      showChangeCaseStatus = changeCaseStatus,
      showTakeOffReferral = takeOffReferral,
      showReopen = showReopenButton,
      caseStatus = c.status
    )
  }
}
