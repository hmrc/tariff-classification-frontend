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

import java.time.{Duration, ZonedDateTime}

case class Case
(
  reference: String,
  status: String,
  createdDate: ZonedDateTime = ZonedDateTime.now(),
  adjustedCreateDate: ZonedDateTime = ZonedDateTime.now(),
  closedDate: Option[ZonedDateTime] = None,
  caseBoardsFileNumber: Option[String] = None,
  assigneeId: Option[String] = None,
  queueId: String = "gateway",
  application: Application,
  decision: Option[Decision] = None,
  attachments: Seq[Attachment] = Seq.empty
) {
  def elapsedDays: Long = {
    if(closedDate.isEmpty) {
      Duration.between(adjustedCreateDate, ZonedDateTime.now()).toDays
    } else {
      Duration.between(adjustedCreateDate, closedDate.get).toDays
    }
  }
}
