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

package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.Queue

class SLAReportTest extends UnitSpec {

  def newReportContaining(groups: ((Queue, HistogramBucketInterval), Seq[Int])*): SLAReport = {
    val map: Map[(Option[String], HistogramBucketInterval), HistogramBucket] = groups.map {
      case ((queue, interval), data) => ((Some(queue.id), interval), HistogramBucket(data))
    } toMap
    val histogram = Histogram(map)
    new SLAReport(histogram)
  }

  "SLA Report" should {
    val queue1 = Queue("q1", "q1-slug", "q1-name")
    val queue2 = Queue("q2", "q2-slug", "q2-name")
    val queue3 = Queue("q3", "q3-slug", "q3-name")
    val interval1 = HistogramBucketInterval(None, 1) // x <= 1
    val interval2 = HistogramBucketInterval(2, 3) // 2 <= x <= 3
    val interval3 = HistogramBucketInterval(4, None) // x >= 4

    "Calculate total count" in {
      // Given
      newReportContaining(
        (queue1, interval1) -> Seq(0, 1),
        (queue1, interval2) -> Seq(2, 3)
      ).totalCount shouldBe 4
    }

    "Calculate total count by queue" in {
      val report = newReportContaining(
        (queue1, interval1) -> Seq(0, 1),
        (queue1, interval2) -> Seq(2, 3),
        (queue2, interval1) -> Seq(0, 1)
      )
      report.totalCountFor(queue1) shouldBe 4
      report.totalCountFor(queue2) shouldBe 2
      report.totalCountFor(queue3) shouldBe 0
    }

    "Calculate total count by interval" in {
      val report = newReportContaining(
        (queue1, interval1) -> Seq(0, 1),
        (queue1, interval2) -> Seq(2, 3),
        (queue2, interval1) -> Seq(0, 1)
      )
      report.totalCountFor(interval1) shouldBe 4
      report.totalCountFor(interval2) shouldBe 2
      report.totalCountFor(interval3) shouldBe 0
    }

    "Calculate total percent by interval" in {
      val report = newReportContaining(
        (queue1, interval1) -> Seq(0, 1),
        (queue1, interval2) -> Seq(2, 3),
        (queue2, interval1) -> Seq(0, 1)
      )
      report.totalPercentFor(interval1) shouldBe 67
      report.totalPercentFor(interval2) shouldBe 33
      report.totalPercentFor(interval3) shouldBe 0
    }

    "Calculate total count by queue and interval" in {
      val report = newReportContaining(
        (queue1, interval1) -> Seq(0, 1),
        (queue1, interval2) -> Seq(2, 3),
        (queue2, interval1) -> Seq(0, 1)
      )
      report.totalCountFor(queue1, interval1) shouldBe 2
      report.totalCountFor(queue1, interval2) shouldBe 2
      report.totalCountFor(queue1, interval3) shouldBe 0

      report.totalCountFor(queue2, interval1) shouldBe 2
      report.totalCountFor(queue2, interval2) shouldBe 0

      report.totalCountFor(queue3, interval1) shouldBe 0
    }

    "Calculate total percent by queue and interval" in {
      val report = newReportContaining(
        (queue1, interval1) -> Seq(0, 1),
        (queue1, interval2) -> Seq(2, 3),
        (queue2, interval1) -> Seq(0, 1)
      )
      report.totalPercentFor(queue1, interval1) shouldBe 50
      report.totalPercentFor(queue1, interval2) shouldBe 50
      report.totalPercentFor(queue1, interval3) shouldBe 0

      report.totalPercentFor(queue2, interval1) shouldBe 100
      report.totalPercentFor(queue2, interval2) shouldBe 0

      report.totalPercentFor(queue3, interval1) shouldBe 0
    }
  }

}
