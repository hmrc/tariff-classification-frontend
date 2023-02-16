/*
 * Copyright 2023 HM Revenue & Customs
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

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventsService @Inject() (connector: BindingTariffClassificationConnector, auditService: AuditService)(
  implicit ec: ExecutionContext
) {

  def getEvents(reference: String, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Event]] =
    getFilteredEvents(reference, pagination, None)

  def findReferralEvents(references: Set[String])(
    implicit hc: HeaderCarrier
  ): Future[Map[String, Event]] =
    connector.findReferralEvents(references)

  def findCompletionEvents(references: Set[String])(
    implicit hc: HeaderCarrier
  ): Future[Map[String, Event]] =
    connector.findCompletionEvents(references)

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
