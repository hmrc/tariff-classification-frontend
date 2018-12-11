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

package uk.gov.hmrc.tariffclassificationfrontend.service

import java.time.{Clock, ZonedDateTime}

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CasesService @Inject()(appConfig: AppConfig, auditService: AuditService, connector: BindingTariffClassificationConnector) {

  private def addEvent(original: Case, updated: Case, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] = {
    val event = NewEventRequest(CaseStatusChange(original.status, updated.status), operator.id)
    connector.createEvent(updated, event)
      .map(_ => Unit)
      .recover({
        case throwable: Throwable =>
          Logger.error(s"Could not create Event for case [${original.reference}] with payload [$event]", throwable)
      })
      .map(_ => updated)
  }

  def releaseCase(original: Case, queue: Queue, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] = {
    connector.updateCase(original.copy(status = CaseStatus.OPEN, queueId = Some(queue.id)))
      .flatMap((updated: Case) => {
        auditService.auditCaseReleased(updated)
        addEvent(original, updated, operator)
      })
  }

  def completeCase(original: Case, operator: Operator, clock: Clock = Clock.systemDefaultZone())(implicit hc: HeaderCarrier): Future[Case] = {
    val startDate = ZonedDateTime.now(clock)
    val endDate = startDate.plusYears(appConfig.decisionLifetimeYears)

    val decisionUpdating: Decision = original.decision
      .getOrElse(throw new IllegalArgumentException("Cannot Complete a Case without a Decision"))
      .copy(effectiveStartDate = startDate, effectiveEndDate = endDate)
    val caseUpdating = original.copy(status = CaseStatus.COMPLETED, decision = Some(decisionUpdating))

    connector.updateCase(caseUpdating)
      .flatMap((updated: Case) => {
        auditService.auditCaseCompleted(updated)
        addEvent(original, updated, operator)
      })
  }

  def getOne(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] = {
    connector.findCase(reference)
  }

  def getCasesByQueue(queue: Queue)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    connector.findCasesByQueue(queue)
  }

  def getCasesByAssignee(assignee: String)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    connector.findCasesByAssignee(assignee)
  }

  def updateCase(caseToUpdate: Case)(implicit hc: HeaderCarrier): Future[Case] = {
    connector.updateCase(caseToUpdate)
  }

}
