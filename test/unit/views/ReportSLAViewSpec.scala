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

package views

import java.time.Instant

import models._
import views.ViewMatchers._

class ReportSLAViewSpec extends ViewSpec {

  private val range = InstantRange(min = Instant.EPOCH, max = Instant.EPOCH.plusSeconds(86400))
  private val queue1 = Queue("1", "Q1", "Queue 1 Name")
  private val queue2 = Queue("2", "Q2", "Queue 2 Name")

  "Report SLA View" should {

    "render To and From dates" in {
      // When
      val doc = view(html.report_sla(range, Seq.empty[ReportResult], Seq.empty[Queue]))

      // Then
      doc should containElementWithID("report_sla-from_date")
      doc.getElementById("report_sla-from_date") should containText("")
      doc should containElementWithID("report_sla-to_date")
      doc.getElementById("report_sla-to_date") should containText("")
    }

    "render intervals" in {
      // When
      val doc = view(html.report_sla(range, Seq.empty[ReportResult], Seq.empty[Queue]))

      // Then
      doc should containElementWithID("report_sla-table-intervals")
      doc.getElementById("report_sla-table-interval_0") should containText("0 - 10")
      doc.getElementById("report_sla-table-interval_1") should containText("11 - 20")
      doc.getElementById("report_sla-table-interval_2") should containText("21 - 30")
      doc.getElementById("report_sla-table-interval_3") should containText("31 - 40")
      doc.getElementById("report_sla-table-interval_4") should containText("41 - 50")
      doc.getElementById("report_sla-table-interval_5") should containText("51 +")
    }

    "render queues - with no data" in {
      // When
      val doc = view(html.report_sla(range, Seq.empty[ReportResult], Seq(queue1, queue2)))

      // Then
      doc should containElementWithID(s"report_sla-table-queue_${queue1.slug}")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-name") should containText(queue1.name)
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_0-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_0-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_1-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_1-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_2-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_2-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_3-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_3-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_4-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_4-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_5-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_5-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-total") should containText("0")

      doc should containElementWithID(s"report_sla-table-queue_${queue2.slug}")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-name") should containText(queue2.name)
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_0-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_0-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_1-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_1-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_2-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_2-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_3-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_3-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_4-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_4-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_5-count") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_5-percent") should containText("0")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-total") should containText("0")
    }

    "render queues - with some data" in {
      // Given
      val results = Seq(
        ReportResult(Map(CaseReportGroup.QUEUE -> Some(queue1.id)), Seq(0, 11, 21, 31, 41, 51)),
        ReportResult(Map(CaseReportGroup.QUEUE -> Some(queue2.id)), Seq(10, 20, 30, 40, 50, 60))
      )

      // When
      val doc = view(html.report_sla(range, results, Seq(queue1, queue2)))

      // Then
      doc should containElementWithID(s"report_sla-table-queue_${queue1.slug}")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-name") should containText(queue1.name)
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_0-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_0-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_1-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_1-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_2-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_2-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_3-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_3-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_4-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_4-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_5-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-interval_5-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue1.slug}-total") should containText("6")

      doc should containElementWithID(s"report_sla-table-queue_${queue2.slug}")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-name") should containText(queue2.name)
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_0-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_0-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_1-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_1-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_2-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_2-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_3-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_3-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_4-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_4-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_5-count") should containText("1")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-interval_5-percent") should containText("17")
      doc.getElementById(s"report_sla-table-queue_${queue2.slug}-total") should containText("6")
    }

    "render totals - with no data" in {
      // When
      val doc = view(html.report_sla(range, Seq.empty[ReportResult], Seq(queue1, queue2)))

      // Then
      doc should containElementWithID("report_sla-table-totals")
      doc.getElementById("report_sla-table-totals-interval_0-count") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_0-percent") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_1-count") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_1-percent") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_2-count") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_2-percent") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_3-count") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_3-percent") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_4-count") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_4-percent") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_5-count") should containText("0")
      doc.getElementById("report_sla-table-totals-interval_5-percent") should containText("0")
      doc.getElementById("report_sla-table-totals-total") should containText("0")
    }

    "render totals - with some data" in {
      // Given
      val results = Seq(
        ReportResult(Map(CaseReportGroup.QUEUE -> Some(queue1.id)), Seq(0, 11, 21, 31, 41, 51)),
        ReportResult(Map(CaseReportGroup.QUEUE -> Some(queue2.id)), Seq(10, 20, 30, 40, 50, 60))
      )

      // When
      val doc = view(html.report_sla(range, results, Seq(queue1, queue2)))

      // Then
      doc should containElementWithID("report_sla-table-totals")
      doc.getElementById("report_sla-table-totals-interval_0-count") should containText("2")
      doc.getElementById("report_sla-table-totals-interval_0-percent") should containText("17")
      doc.getElementById("report_sla-table-totals-interval_1-count") should containText("2")
      doc.getElementById("report_sla-table-totals-interval_1-percent") should containText("17")
      doc.getElementById("report_sla-table-totals-interval_2-count") should containText("2")
      doc.getElementById("report_sla-table-totals-interval_2-percent") should containText("17")
      doc.getElementById("report_sla-table-totals-interval_3-count") should containText("2")
      doc.getElementById("report_sla-table-totals-interval_3-percent") should containText("17")
      doc.getElementById("report_sla-table-totals-interval_4-count") should containText("2")
      doc.getElementById("report_sla-table-totals-interval_4-percent") should containText("17")
      doc.getElementById("report_sla-table-totals-interval_5-count") should containText("2")
      doc.getElementById("report_sla-table-totals-interval_5-percent") should containText("17")
      doc.getElementById("report_sla-table-totals-total") should containText("12")
    }
  }

}
