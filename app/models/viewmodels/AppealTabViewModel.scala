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

package models.viewmodels

import models._

case class AppealTabViewModel(
  caseReference: String,
  appeals: Seq[Appeal],
  applicationForExtendedUseButton: Option[String],
  permissionForExtendedUse: Boolean
)

object AppealTabViewModel {

  def fromCase(c: Case, operator: Operator): AppealTabViewModel = {

    val appealsDecision = c.decision.map(_.appeal).getOrElse(Seq.empty)

    val applicationForExtendedUse = c.decision.flatMap(_.cancellation).exists(_.applicationForExtendedUse)

    val showExtendedUseButton =
      c.status == CaseStatus.CANCELLED && operator.permissions.contains(Permission.APPEAL_CASE)

    val yesNoLink: Option[String] =
      if (c.status == CaseStatus.CANCELLED && applicationForExtendedUse) Some("Yes")
      else if (c.status == CaseStatus.CANCELLED && !applicationForExtendedUse) Some("No")
      else None

    AppealTabViewModel(c.reference, appealsDecision, applicationForExtendedUseButton = yesNoLink, showExtendedUseButton)
  }
}
