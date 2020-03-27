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
                               c592ViewModel: C592ViewModel,
                               activityViewModel: ActivityViewModel,
                               isNewCase: Boolean,
                               hasPermissions: Boolean
                             )

object LiabilityViewModel {

  def fromCase(c: Case, operator: Operator, events: Paged[Event], queues: Seq[Queue]) = {

    def status = {
      c.status match {
        case CaseStatus.CANCELLED => s"CANCELLED${c.decision.flatMap(_.cancellation).map(c => CancelReason.code(c.reason)).map(s => s" - $s").getOrElse("")}"
        case CaseStatus.COMPLETED if c.hasExpiredRuling => "EXPIRED"
        case s: CaseStatus => s.toString
      }
    }

    def isNew: Boolean = c.status == CaseStatus.NEW

    def releaseOrSuppressPermissions =
      operator.permissions.contains(Permission.RELEASE_CASE) || operator.permissions.contains(Permission.SUPPRESS_CASE)


    LiabilityViewModel(
      CaseHeaderViewModel("Liability", c.application.businessName, c.application.goodsName, c.reference, status, c.application.isLiveLiabilityOrder),
      C592ViewModel.fromCase(c),
      ActivityViewModel.fromCase(c, events, queues),
      isNewCase = isNew,
      hasPermissions = releaseOrSuppressPermissions
    )
  }
}