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

package views.managementtools

import cats.data.NonEmptySeq
import models._
import models.reporting._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.summaryReportTable

class SummaryReportTableViewSpec extends ViewSpec {

  "summaryReportTable view" should {
    val report = SummaryReport(
      "Case count by status",
      groupBy   = NonEmptySeq.one(ReportField.Status),
      sortBy    = ReportField.Status,
      maxFields = Seq(ReportField.ElapsedDays)
    )

    val reportResults: Paged[ResultGroup] = Paged(
      Seq(
        SimpleResultGroup(
          2,
          NonEmptySeq.one(StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.COMPLETED))),
          List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(5)))
        ),
        SimpleResultGroup(
          4,
          NonEmptySeq.one(StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.CANCELLED))),
          List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(2)))
        ),
        SimpleResultGroup(
          6,
          NonEmptySeq.one(StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.OPEN))),
          List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(8)))
        ),
        SimpleResultGroup(
          7,
          NonEmptySeq.one(StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.NEW))),
          List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(4)))
        )
      )
    )

    "render a header for each field" in {
      val doc =
        view(summaryReportTable(report, SearchPagination(), reportResults, Map.empty, Map.empty, "summary-report"))

      for (field <- ReportField.Count :: ReportField.Status :: report.maxFields.toList) {
        doc should containElementWithID(s"summary-report-${field.fieldName}")
      }

      doc.getElementById("summary-report-count")        should containText(messages("reporting.field.count"))
      doc.getElementById("summary-report-status")       should containText(messages("reporting.field.status"))
      doc.getElementById("summary-report-elapsed_days") should containText(messages("reporting.field.elapsed_days"))
    }

    "render data for each row" in {
      val doc =
        view(summaryReportTable(report, SearchPagination(), reportResults, Map.empty, Map.empty, "summary-report"))

      for ((_, idx) <- reportResults.results.zipWithIndex) {
        doc should containElementWithID(s"summary-report-count-$idx")
        doc should containElementWithID(s"summary-report-status-$idx")
        doc should containElementWithID(s"summary-report-elapsed_days-$idx")
      }

      doc.getElementById("summary-report-count-0")        should containText("2")
      doc.getElementById("summary-report-status-0")       should containText("COMPLETED")
      doc.getElementById("summary-report-elapsed_days-0") should containText("5")

      doc.getElementById("summary-report-count-1")        should containText("4")
      doc.getElementById("summary-report-status-1")       should containText("CANCELLED")
      doc.getElementById("summary-report-elapsed_days-1") should containText("2")

      doc.getElementById("summary-report-count-2")        should containText("6")
      doc.getElementById("summary-report-status-2")       should containText("OPEN")
      doc.getElementById("summary-report-elapsed_days-2") should containText("8")

      doc.getElementById("summary-report-count-3")        should containText("7")
      doc.getElementById("summary-report-status-3")       should containText("NEW")
      doc.getElementById("summary-report-elapsed_days-3") should containText("4")
    }

  }
}
