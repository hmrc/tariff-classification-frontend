/*
 * Copyright 2020 HM Revenue & Customs
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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ReportingServiceTest extends ServiceSpecBase with BeforeAndAfterEach {

  private val connector = mock[BindingTariffClassificationConnector]
  private val service = new ReportingService(connector)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
  }

  "Reporting Service" should {
    "Build & Request SLA Report" in {
      val dateRange = mock[InstantRange]
      given(connector.generateReport(any[CaseReport])(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[ReportResult])

      await(service.getSLAReport(dateRange)) shouldBe Seq.empty[ReportResult]

      theReport shouldBe CaseReport(
        filter = CaseReportFilter(
          decisionStartDate = Some(dateRange), applicationType = Some(Set("BTI"))
        ),
        group = Set(CaseReportGroup.QUEUE),
        field = CaseReportField.ACTIVE_DAYS_ELAPSED
      )
    }

    "Build & Request Queue Report" in {
      given(connector.generateReport(any[CaseReport])(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[ReportResult])

      await(service.getQueueReport(mock[HeaderCarrier])) shouldBe Seq.empty[ReportResult]

      theReport shouldBe CaseReport(
        filter = CaseReportFilter(
          status = Some(Set("NEW", "OPEN", "REFERRED", "SUSPENDED")), assigneeId = Some("none")
        ),
        group = Set(CaseReportGroup.QUEUE, CaseReportGroup.APPLICATION_TYPE),
        field = CaseReportField.ACTIVE_DAYS_ELAPSED
      )
    }

    "Build & Request Referral Report" in {
      val dateRange = mock[InstantRange]
      given(connector.generateReport(any[CaseReport])(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[ReportResult])

      await(service.getReferralReport(dateRange)) shouldBe Seq.empty[ReportResult]

      theReport shouldBe CaseReport(
        filter = CaseReportFilter(
          referralDate = Some(dateRange), applicationType = Some(Set("BTI"))
        ),
        group = Set(CaseReportGroup.QUEUE),
        field = CaseReportField.REFERRED_DAYS_ELAPSED
      )
    }

    def theReport: CaseReport = {
      val captor: ArgumentCaptor[CaseReport] = ArgumentCaptor.forClass(classOf[CaseReport])
      verify(connector).generateReport(captor.capture())(any[HeaderCarrier])
      captor.getValue
    }
  }

}
