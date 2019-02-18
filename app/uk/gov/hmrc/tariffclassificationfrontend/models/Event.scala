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
}

sealed trait StatusChange[T] extends Details {
  val from: T
  val to: T
  val comment: Option[String]
}

case class CaseStatusChange
(
  override val from: CaseStatus,
  override val to: CaseStatus,
  override val comment: Option[String] = None
) extends StatusChange[CaseStatus] {
  override val `type`: EventType.Value = EventType.CASE_STATUS_CHANGE
}

case class AppealStatusChange
(
  override val from: Option[AppealStatus],
  override val to: Option[AppealStatus],
  override val comment: Option[String] = None
) extends StatusChange[Option[AppealStatus]] {
  override val `type`: EventType.Value = EventType.APPEAL_STATUS_CHANGE
}

case class ReviewStatusChange
(
  override val from: Option[ReviewStatus],
  override val to: Option[ReviewStatus],
  override val comment: Option[String] = None
) extends StatusChange[Option[ReviewStatus]] {
  override val `type`: EventType.Value = EventType.REVIEW_STATUS_CHANGE
}

case class ExtendedUseStatusChange
(
  override val from: Boolean,
  override val to: Boolean,
  override val comment: Option[String] = None
) extends StatusChange[Boolean] {
  override val `type`: EventType.Value = EventType.EXTENDED_USE_STATUS_CHANGE
}

case class Note
(
  comment: String
) extends Details {
  override val `type`: EventType.Value = EventType.NOTE
}


object EventType extends Enumeration {
  type EventType = Value
  val CASE_STATUS_CHANGE, APPEAL_STATUS_CHANGE, REVIEW_STATUS_CHANGE, EXTENDED_USE_STATUS_CHANGE, NOTE = Value
}
