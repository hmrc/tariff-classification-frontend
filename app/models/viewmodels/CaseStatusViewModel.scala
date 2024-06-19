/*
 * Copyright 2024 HM Revenue & Customs
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

package models
package viewmodels

case class StatusTagViewModel(
  status: String,
  colour: String
)

object StatusTagViewModel {
  def caseStatus(c: Case): StatusTagViewModel = c.status match {
    case CaseStatus.OPEN                             => StatusTagViewModel(c.status.toString, "blue")
    case CaseStatus.COMPLETED if c.hasExpiredRuling  => StatusTagViewModel("EXPIRED", "green")
    case CaseStatus.COMPLETED                        => StatusTagViewModel(c.status.toString, "green")
    case CaseStatus.REFERRED | CaseStatus.SUPPRESSED => StatusTagViewModel(c.status.toString, "yellow")
    case CaseStatus.REJECTED                         => StatusTagViewModel(c.status.toString, "red")
    case CaseStatus.CANCELLED                        => StatusTagViewModel(CaseStatus.formatCancellation(c), "red")
    case _                                           => StatusTagViewModel(c.status.toString, "green")
  }

  def liabilityType(application: Application): Option[StatusTagViewModel] =
    if (application.isLiabilityOrder && application.asLiabilityOrder.isLiveLiabilityOrder) {
      Some(StatusTagViewModel(application.asLiabilityOrder.status.toString, "pink"))
    } else {
      None
    }

  def appealStatus(decision: Option[Decision]): Option[StatusTagViewModel] =
    Appeal.highestAppealFromDecision(decision).map { appeal =>
      StatusTagViewModel(AppealStatus.format(appeal.`type`, appeal.status).toUpperCase, "red")
    }
}

case class CaseStatusViewModel(
  liabilityTypeTag: Option[StatusTagViewModel],
  caseStatusTag: Option[StatusTagViewModel],
  appealStatusTag: Option[StatusTagViewModel]
)

object CaseStatusViewModel {
  def fromCase(c: Case): CaseStatusViewModel = CaseStatusViewModel(
    StatusTagViewModel.liabilityType(c.application),
    Some(StatusTagViewModel.caseStatus(c)),
    StatusTagViewModel.appealStatus(c.decision)
  )
}
