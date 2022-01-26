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

package utils

import java.time.Instant

import models._
import models.request.NewEventRequest

object Events {
  val event: Event = Event("id", Note("comment"), Operator("user-id", Some("user name")), "case-ref", Instant.now())
  val eventRequest: NewEventRequest =
    NewEventRequest(Note("comment"), Operator("user-id", Some("user name")), Instant.now())
  val events: Seq[Event] = Seq(event)

  val referredDetailsCaseChange: ReferralCaseStatusChange =
    ReferralCaseStatusChange(CaseStatus.OPEN, Some("some comment"), None, "trader", Seq(ReferralReason.REQUEST_SAMPLE))

  val referredEvent: Event = event.copy(details = referredDetailsCaseChange)

  val pagedReferredEvents: Paged[Event] = Paged(Seq(referredEvent, referredEvent))
  val referralEventsById: Map[String, Event] = Map(referredEvent.caseReference -> referredEvent)

  val completedDetailsCaseChange: CompletedCaseStatusChange =
    CompletedCaseStatusChange(CaseStatus.OPEN, Some("some comment"), Some("email@email.com"))

  val completedEvent: Event = event.copy(details = completedDetailsCaseChange)

  val pagedCompletedEvents: Paged[Event] = Paged(Seq(completedEvent, completedEvent))
  val completionEventsById: Map[String, Event] = Map(completedEvent.caseReference -> completedEvent)

  val sampleEvent = Event(
    "id",
    SampleStatusChange(None, Some(SampleStatus.SENT_FOR_ANALYSIS)),
    Operator("user-id", Some("user name")),
    "case-ref",
    Instant.now()
  )

  val sampleEvents = Seq(sampleEvent)
}
