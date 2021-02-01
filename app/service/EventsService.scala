/*
 * Copyright 2021 HM Revenue & Customs
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

package service

import java.time.{Clock, Instant}

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import audit.AuditService
import connector.BindingTariffClassificationConnector
import models.EventType.EventType
import models._
import models.request.NewEventRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EventsService @Inject() (connector: BindingTariffClassificationConnector, auditService: AuditService) {

  def getEvents(reference: String, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Event]] =
    getFilteredEvents(reference, pagination, None)

  def findCompletionEvents(references: Set[String], pagination: Pagination)(
    implicit hc: HeaderCarrier
  ): Future[Paged[Event]] =
    connector.findCompletionEvents(references, pagination)

  def getFilteredEvents(reference: String, pagination: Pagination, onlyEventTypes: Option[Set[EventType]])(
    implicit hc: HeaderCarrier
  ): Future[Paged[Event]] =
    connector.findFilteredEvents(reference, pagination, onlyEventTypes.getOrElse(Set.empty))

  def addNote(c: Case, note: String, operator: Operator, clock: Clock = Clock.systemUTC())(
    implicit hc: HeaderCarrier
  ): Future[Event] = {
    val event = NewEventRequest(Note(note), operator, Instant.now(clock))

    connector.createEvent(c, event).map { e =>
      auditService.auditNote(c, note, operator)
      e
    }
  }

}
