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

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CasesService @Inject()(connector: BindingTariffClassificationConnector) {

  def releaseCase(c: Case, queue: Queue, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] = {
    val eventualCase: Future[Case] = connector.updateCase(c.copy(status = CaseStatus.OPEN, queueId = Some(queue.id)))
    eventualCase.onSuccess({
      case updated =>
        connector.createEvent(updated, NewEventRequest(CaseStatusChange(c.status, updated.status), operator.id))
          .onFailure({
            case throwable: Throwable => Logger.error(s"Could not create Release Case event for case [${c.reference}]", throwable)
          })
    })
    eventualCase
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
