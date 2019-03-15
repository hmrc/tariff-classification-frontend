/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.Instant

import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus.AppealStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.EventType.EventType
import uk.gov.hmrc.tariffclassificationfrontend.models.ReviewStatus.ReviewStatus


case class Event
(
  id: String,
  details: Details,
  operator: Operator,
  caseReference: String,
  timestamp: Instant = Instant.now()
)

sealed trait Details {
  val `type`: EventType
  def summary: String
}

sealed trait FieldChange[T] extends Details {
  val from: T
  val to: T
  val comment: Option[String]
}

case class CaseStatusChange
(
  override val from: CaseStatus,
  override val to: CaseStatus,
  override val comment: Option[String] = None
) extends FieldChange[CaseStatus] {
  override val `type`: EventType.Value = EventType.CASE_STATUS_CHANGE
  override def summary: String = s"Status changed from ${from.toString.toLowerCase} to ${to.toString.toLowerCase}"
}

case class AppealStatusChange
(
  override val from: Option[AppealStatus],
  override val to: Option[AppealStatus],
  override val comment: Option[String] = None
) extends FieldChange[Option[AppealStatus]] {
  override val `type`: EventType.Value = EventType.APPEAL_STATUS_CHANGE
  override def summary: String = s"Appeal status changed from ${AppealStatus.format(from)} to ${AppealStatus.format(to)}"
}

case class ReviewStatusChange
(
  override val from: Option[ReviewStatus],
  override val to: Option[ReviewStatus],
  override val comment: Option[String] = None
) extends FieldChange[Option[ReviewStatus]] {
  override val `type`: EventType.Value = EventType.REVIEW_STATUS_CHANGE
  override def summary: String = s"Review status changed from ${ReviewStatus.format(from)} to ${ReviewStatus.format(to)}"
}

case class ExtendedUseStatusChange
(
  override val from: Boolean,
  override val to: Boolean,
  override val comment: Option[String] = None
) extends FieldChange[Boolean] {
  override val `type`: EventType.Value = EventType.EXTENDED_USE_STATUS_CHANGE
  override def summary: String = s"Application for extended use status changed from $from to $to"
}

case class AssignmentChange
(
  override val from: Option[Operator],
  override val to: Option[Operator],
  override val comment: Option[String] = None
) extends FieldChange[Option[Operator]] {
  override val `type`: EventType.Value = EventType.ASSIGNMENT_CHANGE
  override def summary: String = (from, to) match {
    case (Some(from: Operator), Some(to: Operator)) =>
      s"Case reassigned from $from to $to"
    case (None, Some(to: Operator)) =>
      s"Case assigned to $to"
    case (Some(from: Operator), None) =>
      s"Case unassigned from $from"
    case _ =>
      "Case unassigned from unknown operator"
  }
}

case class QueueChange
(
  override val from: Option[String],
  override val to: Option[String],
  override val comment: Option[String] = None
) extends FieldChange[Option[String]] {
  override val `type`: EventType.Value = EventType.QUEUE_CHANGE
  override def summary: String = s"Reassigned this case to the $to queue"
}

case class Note
(
  comment: String
) extends Details {
  override val `type`: EventType.Value = EventType.NOTE
  override def summary: String = "Case note added"
}


object EventType extends Enumeration {
  type EventType = Value
  val CASE_STATUS_CHANGE, APPEAL_STATUS_CHANGE, REVIEW_STATUS_CHANGE, EXTENDED_USE_STATUS_CHANGE, ASSIGNMENT_CHANGE, QUEUE_CHANGE, NOTE = Value
}
