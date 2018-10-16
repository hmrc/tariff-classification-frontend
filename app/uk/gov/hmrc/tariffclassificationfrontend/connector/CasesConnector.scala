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
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.tariffclassificationfrontend.config.{AppConfig, WSHttp}
import uk.gov.hmrc.tariffclassificationfrontend.models.Case

import scala.concurrent.Future

@Singleton
class CasesConnector @Inject()(configuration: AppConfig, client: WSHttp) {

  implicit val hc = HeaderCarrier()
  implicit val reads = Json.format[Case]

  def getGatewayCases: Future[Seq[Case]] = {
    val url = configuration.bindingTariffClassificationUrl + "/cases?queue_id=gateway&assignee_id=none&sort-by=elapsed-days"
    client.GET[Seq[Case]](url)
      .fallbackTo(Future.successful(TemporaryData.CASES))
  }

}
