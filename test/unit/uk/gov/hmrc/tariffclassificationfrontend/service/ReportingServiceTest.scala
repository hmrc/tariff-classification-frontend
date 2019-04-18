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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._

import scala.concurrent.Future

class ReportingServiceTest extends UnitSpec with MockitoSugar {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val connector = mock[BindingTariffClassificationConnector]
  private val service = new ReportingService(connector)

  "Reporting Service" should {
    "Build & Request SLA Report" in {
      val dateRange = mock[InstantRange]
      given(connector.generateReport(any[CaseReport])(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[ReportResult])

      await(service.getSLAReport(dateRange)) shouldBe Seq.empty[ReportResult]

      theReport shouldBe CaseReport(
        filter = CaseReportFilter(
          decisionStartDate = Some(dateRange)
        ),
        group = CaseReportGroup.QUEUE,
        field = CaseReportField.DAYS_ELAPSED
      )
    }

    def theReport: CaseReport = {
      val captor: ArgumentCaptor[CaseReport] = ArgumentCaptor.forClass(classOf[CaseReport])
      verify(connector).generateReport(captor.capture())(any[HeaderCarrier])
      captor.getValue
    }
  }

}
