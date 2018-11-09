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

package uk.gov.hmrc.tariffclassificationfrontend.connector

import com.google.inject.Inject
import javax.inject.Singleton
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Queue, Queues, Status => StatusOfTheCase }
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.{caseFormat, statusFormat}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class BindingTariffClassificationConnector @Inject()(configuration: AppConfig, client: HttpClient) {

  def getOneCase(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] = {
    val url = s"${configuration.bindingTariffClassificationUrl}/cases/$reference"
    client.GET[Option[Case]](url)
  }

  def getCasesByQueue(queue: Queue)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    val queueId = if (queue == Queues.gateway) "none" else queue.id
    val queryString = s"queue_id=$queueId&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"
    val url = s"${configuration.bindingTariffClassificationUrl}/cases?$queryString"
    client.GET[Seq[Case]](url)
  }

  def getCasesByAssignee(assignee: String)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    val queryString = s"assignee_id=$assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"
    val url = s"${configuration.bindingTariffClassificationUrl}/cases?$queryString"
    client.GET[Seq[Case]](url)
  }

  def updateCaseStatus(caseReference: String, newStatus: CaseStatus)(implicit hc: HeaderCarrier): Future[Case] = {
    val url = s"${configuration.bindingTariffClassificationUrl}/cases/$caseReference/status"
    client.PUT[StatusOfTheCase, Case](url, body = StatusOfTheCase(newStatus))
  }

  // TODO: DIT-246 - we should not have this method, but instead have a PUT /cases/:reference/queueId for updating the `queueId` only
  def updateCase(c: Case)(implicit hc: HeaderCarrier): Future[Case] = {
    val url = s"${configuration.bindingTariffClassificationUrl}/cases/${c.reference}"
    client.PUT[Case, Case](url, body = c)
  }

}
