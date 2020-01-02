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

package uk.gov.hmrc.tariffclassificationfrontend.views

import java.time.Instant

import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._

class ReportReferralViewSpec extends ViewSpec {

  private val range = InstantRange(min = Instant.EPOCH, max = Instant.EPOCH.plusSeconds(86400))
  private val queue1 = Queue("1", "Q1", "Queue 1 Name")
  private val queue2 = Queue("2", "Q2", "Queue 2 Name")

  "Report Referral View" should {

    "render To and From dates" in {
      // When
      val doc = view(html.report_referral(range, Seq.empty[ReportResult], Seq.empty[Queue]))

      // Then
      doc should containElementWithID("report_referral-from_date")
      doc.getElementById("report_referral-from_date") should containText("")
      doc should containElementWithID("report_referral-to_date")
      doc.getElementById("report_referral-to_date") should containText("")
    }

    "render queues - with no data" in {
      // When
      val doc = view(html.report_referral(range, Seq.empty[ReportResult], Seq(queue1, queue2)))

      // Then
      doc should containElementWithID(s"report_referral-table-queue_${queue1.slug}")
      doc.getElementById(s"report_referral-table-queue_${queue1.slug}-name") should containText(queue1.name)
      doc.getElementById(s"report_referral-table-queue_${queue1.slug}-total") should containText("0")
      doc.getElementById(s"report_referral-table-queue_${queue1.slug}-average") should containText("0")

      doc should containElementWithID(s"report_referral-table-queue_${queue2.slug}")
      doc.getElementById(s"report_referral-table-queue_${queue2.slug}-name") should containText(queue2.name)
      doc.getElementById(s"report_referral-table-queue_${queue2.slug}-total") should containText("0")
      doc.getElementById(s"report_referral-table-queue_${queue2.slug}-average") should containText("0")
    }

    "render queues - with some data" in {
      // Given
      val results = Seq(
        ReportResult(group = Map(CaseReportGroup.QUEUE -> Some(queue1.id)), value = Seq(0, 11, 21, 31, 41, 51)),
        ReportResult(group = Map(CaseReportGroup.QUEUE -> Some(queue2.id)), Seq(10, 20, 30, 40, 50, 60))
      )

      // When
      val doc = view(html.report_referral(range, results, Seq(queue1, queue2)))

      // Then
      doc should containElementWithID(s"report_referral-table-queue_${queue1.slug}")
      doc.getElementById(s"report_referral-table-queue_${queue1.slug}-name") should containText(queue1.name)
      doc.getElementById(s"report_referral-table-queue_${queue1.slug}-total") should containText("6")
      doc.getElementById(s"report_referral-table-queue_${queue1.slug}-average") should containText("26")

      doc should containElementWithID(s"report_referral-table-queue_${queue2.slug}")
      doc.getElementById(s"report_referral-table-queue_${queue2.slug}-name") should containText(queue2.name)
      doc.getElementById(s"report_referral-table-queue_${queue2.slug}-total") should containText("6")
      doc.getElementById(s"report_referral-table-queue_${queue2.slug}-average") should containText("35")
    }

    "render totals - with no data" in {
      // When
      val doc = view(html.report_referral(range, Seq.empty[ReportResult], Seq(queue1, queue2)))

      // Then
      doc should containElementWithID("report_referral-table-totals")
      doc.getElementById("report_referral-table-totals-total") should containText("0")
      doc.getElementById("report_referral-table-totals-average") should containText("0")
    }

    "render totals - with some data" in {
      // Given
      val results = Seq(
        ReportResult(Map(CaseReportGroup.QUEUE -> Some(queue1.id)), Seq(0, 11, 21, 31, 41, 51)),
        ReportResult(Map(CaseReportGroup.QUEUE -> Some(queue2.id)), Seq(10, 20, 30, 40, 50, 60))
      )

      // When
      val doc = view(html.report_referral(range, results, Seq(queue1, queue2)))

      // Then
      doc should containElementWithID("report_referral-table-totals")
      doc.getElementById("report_referral-table-totals-total") should containText("12")
      doc.getElementById("report_referral-table-totals-average") should containText("30")
    }
  }

}
