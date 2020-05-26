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

import models.{Appeal, Case, CaseStatus, Operator, Permission}

case class AppealTabViewModel(
                               caseReference: String,
                               appeals: Seq[Appeal],
                               showApplicationForExtendedUse: Boolean,
                               permissionForExtendedUse: Boolean
                             )

object AppealTabViewModel {

  def fromCase(c: Case, operator: Operator): AppealTabViewModel = {
    def appealCasePermissions: Boolean = operator.permissions.contains(Permission.APPEAL_CASE)

    val appealsDecision = c.decision.map(_.appeal).getOrElse(Seq.empty)
    val x = c.decision.flatMap(_.cancellation).exists(_.applicationForExtendedUse)
    val permissionForExtendedUse = c.status == CaseStatus.CANCELLED && appealCasePermissions

    AppealTabViewModel(c.reference,
      appealsDecision,
      x,
      permissionForExtendedUse)
  }
}


