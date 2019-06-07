/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.{NEW, OPEN, REFERRED, SUSPENDED}
import uk.gov.hmrc.tariffclassificationfrontend.models._

import scala.concurrent.Future

@Singleton
class ReportingService @Inject()(connector: BindingTariffClassificationConnector) {

  def getSLAReport(decisionStartDate: InstantRange)
                  (implicit hc: HeaderCarrier): Future[Seq[ReportResult]] = {
    val report = CaseReport(
      filter = CaseReportFilter(
        decisionStartDate = Some(decisionStartDate), applicationType = Some(Set("BTI"))
      ),
      group = CaseReportGroup.QUEUE,
      field = CaseReportField.ACTIVE_DAYS_ELAPSED
    )

    connector.generateReport(report)
  }

  def getQueueReport(implicit hc: HeaderCarrier): Future[Seq[ReportResult]] = {
    val statuses = Set(NEW, OPEN, REFERRED, SUSPENDED).map(_.toString)

    val report = CaseReport(
      filter = CaseReportFilter(status = Some(statuses), assigneeId = Some("none")),
      group = CaseReportGroup.QUEUE,
      field = CaseReportField.ACTIVE_DAYS_ELAPSED
    )
    connector.generateReport(report)
  }

  def getReferralReport(referralDate: InstantRange)
                       (implicit hc: HeaderCarrier): Future[Seq[ReportResult]] = {
    val report = CaseReport(
      filter = CaseReportFilter(
        referralDate = Some(referralDate), applicationType = Some(Set("BTI"))
      ),
      group = CaseReportGroup.QUEUE,
      field = CaseReportField.REFERRED_DAYS_ELAPSED
    )

    connector.generateReport(report)
  }

}
