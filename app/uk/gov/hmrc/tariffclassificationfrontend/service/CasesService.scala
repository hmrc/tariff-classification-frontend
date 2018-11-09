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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Queue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CasesService @Inject()(connector: BindingTariffClassificationConnector) {

  def releaseCase(c: Case, queue: Queue)(implicit hc: HeaderCarrier): Future[Case] = {
    // TODO: DIT-246 - with an atomic operation we should:
    // - update status
    // - create case status change event
    // If too complex, we should at least update the event and the event atomically
    connector.updateCaseStatus(caseReference = c.reference, newStatus = CaseStatus.OPEN).flatMap { withNewStatus: Case =>
      connector.updateCase(withNewStatus.copy(queueId = Some(queue.id)))
    }
  }

  def getOne(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] = {
    connector.getOneCase(reference)
  }

  def getCasesByQueue(queue: Queue)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    connector.getCasesByQueue(queue)
  }

  def getCasesByAssignee(assignee: String)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    connector.getCasesByAssignee(assignee)
  }

  def updateCase(caseToUpdate: Case)(implicit hc: HeaderCarrier): Future[Case] = {
    connector.updateCase(caseToUpdate)
  }

}
