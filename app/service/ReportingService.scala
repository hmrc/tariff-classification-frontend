/*
 * Copyright 2024 HM Revenue & Customs
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

import connector.BindingTariffClassificationConnector
import models._
import models.reporting._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ReportingService @Inject() (connector: BindingTariffClassificationConnector) {

  def caseReport(report: CaseReport, pagination: Pagination)(using
    hc: HeaderCarrier
  ): Future[Paged[Map[String, ReportResultField[_]]]] =
    connector.caseReport(report, pagination)

  def summaryReport(report: SummaryReport, pagination: Pagination)(using
    hc: HeaderCarrier
  ): Future[Paged[ResultGroup]] =
    connector.summaryReport(report, pagination)

  def queueReport(report: QueueReport, pagination: Pagination)(using
    hc: HeaderCarrier
  ): Future[Paged[QueueResultGroup]] =
    connector.queueReport(report, pagination)
}
