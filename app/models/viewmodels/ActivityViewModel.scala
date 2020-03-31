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

import java.time.Instant

import models.{Case, Event, Operator, Paged, Queue}

case class ActivityViewModel(
                              referenceNumber: String,
                              assignee: Option[Operator],
                              queueId: Option[String],
                              createdDate: Instant,
                              events: Paged[Event],
                              queues: Seq[Queue],
                              queueName: String
                            ) {

}

object ActivityViewModel {
  def fromCase(c: Case, events: Paged[Event], queues: Seq[Queue]): ActivityViewModel = {

   def getQueueName = {

    c.queueId.flatMap(id => queues.find(_.id == id)).map(_.name).getOrElse("unknown")
   }

    ActivityViewModel(c.reference,
      c.assignee,
      c.queueId,
      c.createdDate,
      events,
      queues,
      queueName = getQueueName
    )
  }

}
