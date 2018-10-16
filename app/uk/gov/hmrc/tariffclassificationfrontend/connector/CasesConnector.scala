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

import java.time.LocalDate

import com.google.inject.Inject
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.tariffclassificationfrontend.models.Case

import scala.concurrent.Future

class CasesConnector @Inject()(configuration: Configuration, client: ConnectorHttpClient) {

  implicit val hc = HeaderCarrier()
  implicit val reads = Json.format[Case]

  val case1 = Case("1", "Laptops", "Pol's PCs", LocalDate.of(2018,10,1), "OPEN", "BTI", 1)
  val case2 = Case("2", "Pizza", "Ed's Eatery", LocalDate.of(2018,10,5), "DRAFT", "BTI", 10)
  val case3 = Case("3", "Pasta", "Stefano's Supermarket", LocalDate.of(2018,10,15), "OPEN", "BTI", 5)

  def getGatewayCases(): Future[Seq[Case]] = {
    val baseURL = configuration.getString("client.binding-tariff-classification.base").get
    client.GET[Seq[Case]](baseURL + "/binding-tariff-classification/cases").fallbackTo(Future.successful(Seq(case1, case2, case3)))
  }

}
