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

package models

import java.time.{Clock, Instant}
import models.CaseStatus.CaseStatus

case class Case(
  reference: String,
  status: CaseStatus,
  createdDate: Instant,
  daysElapsed: Long,
  caseBoardsFileNumber: Option[String],
  assignee: Option[Operator],
  queueId: Option[String],
  application: Application,
  decision: Option[Decision],
  attachments: Seq[Attachment],
  keywords: Set[String]             = Set.empty,
  sample: Sample                    = Sample(),
  dateOfExtract: Option[Instant]    = None,
  migratedDaysElapsed: Option[Long] = None,
  referredDaysElapsed: Long
) {
  def hasQueue: Boolean = queueId.isDefined

  def hasStatus(statuses: CaseStatus*): Boolean = statuses.contains(status)

  def hasAssignee: Boolean = assignee.isDefined

  private def hasRuling: Boolean =
    decision.flatMap(_.effectiveEndDate).isDefined

  def hasExpiredRuling(implicit clock: Clock = Clock.systemUTC()): Boolean =
    hasRuling && decision.flatMap(_.effectiveEndDate).exists(_.isBefore(Instant.now(clock)))

  def hasLiveRuling(implicit clock: Clock = Clock.systemUTC()): Boolean =
    hasRuling && !hasExpiredRuling

  def isAssignedTo(operator: Operator): Boolean =
    assignee.exists(_.id == operator.id)

  def findAppeal(appealId: String): Option[Appeal] =
    decision.flatMap(d => d.appeal.find(a => a.id.equals(appealId)))

  def addAttachment(attachment: Attachment): Case = this.copy(attachments = this.attachments :+ attachment)

  def sampleToBeProvided: Boolean =
    application.`type` match {
      case ApplicationType.ATAR      => application.asATAR.sampleToBeProvided
      case ApplicationType.LIABILITY => sample.status.isDefined

    }

  def sampleToBeReturned: Boolean =
    application.`type` match {
      case ApplicationType.ATAR      => application.asATAR.sampleToBeReturned
      case ApplicationType.LIABILITY => sample.returnStatus.contains(SampleReturn.YES)
    }

  def isCaseOverdue: Boolean =
    application.isLiveLiabilityOrder match {
      case true if daysElapsed >= 5   => true
      case false if daysElapsed >= 30 => true
      case _                          => false
    }
}
