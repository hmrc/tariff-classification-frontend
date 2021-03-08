/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterEach
import models._
import models.reporting._
import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.QueryStringBindable
import scala.concurrent.Future
import cats.data.NonEmptySeq

class ReportingServiceTest extends ServiceSpecBase with BeforeAndAfterEach {

  private val connector = mock[BindingTariffClassificationConnector]
  private val service   = new ReportingService(connector)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
  }

  "Reporting Service - caseReport" should {
    "delegate to connector" in {
      given(
        connector.caseReport(any[CaseReport], any[Pagination])(
          any[HeaderCarrier],
          any[QueryStringBindable[CaseReport]],
          any[QueryStringBindable[Pagination]]
        )
      ) willReturn Future.successful(Paged.empty[Map[String, ReportResultField[_]]])

      await(
        service.caseReport(CaseReport("ATaR Summary Report", fields = NonEmptySeq.one(ReportField.Reference)), SearchPagination())
      ) shouldBe Paged.empty
    }
  }

  "Reporting Service - summaryReport" should {
    "delegate to connector" in {
      given(
        connector.summaryReport(any[SummaryReport], any[Pagination])(
          any[HeaderCarrier],
          any[QueryStringBindable[SummaryReport]],
          any[QueryStringBindable[Pagination]]
        )
      ) willReturn Future.successful(Paged.empty[ResultGroup])

      await(
        service.summaryReport(
          SummaryReport(
            "Case count by status",
            groupBy = NonEmptySeq.one(ReportField.Status),
            sortBy  = ReportField.Status
          ),
          SearchPagination()
        )
      ) shouldBe Paged.empty
    }
  }

  "Reporting Service - queueReport" should {
    "delegate to connector" in {
      given(
        connector.queueReport(any[QueueReport], any[Pagination])(
          any[HeaderCarrier],
          any[QueryStringBindable[QueueReport]],
          any[QueryStringBindable[Pagination]]
        )
      ) willReturn Future.successful(Paged.empty[QueueResultGroup])

      await(
        service.queueReport(
          QueueReport(),
          SearchPagination()
        )
      ) shouldBe Paged.empty
    }
  }
}
