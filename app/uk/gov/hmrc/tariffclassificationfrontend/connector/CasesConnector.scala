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
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.caseFormat

import scala.concurrent.Future

@Singleton
class CasesConnector @Inject()(configuration: AppConfig, client: HttpClient) {

  def getOne(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] = {
    val url = s"${configuration.bindingTariffClassificationUrl}/cases/$reference"
    client.GET[Option[Case]](url)
  }

  def getCasesByQueue(queue: Queue)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    val queueId = if (queue.id == 1) "none" else queue.id
    val url = s"${configuration.bindingTariffClassificationUrl}/cases?queue_id=$queueId&assignee_id=none&sort-by=elapsed-days"
    client.GET[Seq[Case]](url)
  }

}
