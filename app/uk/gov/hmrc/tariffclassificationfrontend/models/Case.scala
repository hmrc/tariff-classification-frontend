/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.{Clock, Duration, ZonedDateTime}

case class Case
(
  reference: String,
  status: String,
  createdDate: ZonedDateTime,
  adjustedCreateDate: ZonedDateTime,
  closedDate: Option[ZonedDateTime],
  caseBoardsFileNumber: Option[String],
  assigneeId: Option[String],
  queueId: Option[String],
  application: Application,
  decision: Option[Decision],
  attachments: Seq[Attachment]
) {

  def elapsedDays: Long = {
    elapsedDays(Clock.systemDefaultZone())
  }

  def elapsedDays(clock: Clock): Long = {
    if (closedDate.isEmpty) {
      Duration.between(adjustedCreateDate, ZonedDateTime.now(clock)).toDays
    } else {
      Duration.between(adjustedCreateDate, closedDate.get).toDays
    }
  }

}
