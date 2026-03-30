/*
 * Copyright 2025 HM Revenue & Customs
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
import views.html.managementtools.reportFilters

import java.time.Instant

class ReportFiltersViewSpec extends ViewSpec {
  "reportFilters view" should {
    val summaryReport =
      SummaryReport("Summary report", groupBy = NonEmptySeq.one(ReportField.Status), sortBy = ReportField.Status)
    val caseReport =
      CaseReport(
        "Case report",
        fields = NonEmptySeq.of(ReportField.Reference, ReportField.Status, ReportField.ElapsedDays)
      )

    "show all dates label when report range is not specified" in {
      val doc = view(reportFilters(summaryReport, SearchPagination(1, 25), Queues.allQueuesById))
      doc.getElementById("report_chosen_dates") should containText(
        messages("reporting.view_report.date_range.all_time")
      )
    }

    "show specific dates when report range has been chosen" in {
      val dateRangeReport = caseReport.copy(dateRange =
        InstantRange(
          Instant.parse("2019-01-23T09:00:00.00Z"),
          Instant.parse("2021-04-16T12:00:00.00Z")
        )
      )

      val doc = view(reportFilters(dateRangeReport, SearchPagination(1, 25), Queues.allQueuesById))

      doc.getElementById("report_chosen_dates") should containText(
        messages("reporting.view_report.date_range", "23 Jan 2019", "16 Apr 2021")
      )
    }

    "show correct link to change date range" in {
      val doc = view(reportFilters(summaryReport, SearchPagination(1, 25), Queues.allQueuesById))

      doc.getElementById("report_change_dates").child(0) should haveAttribute(
        "href",
        controllers.routes.ReportingController.showChangeDateFilter(summaryReport, SearchPagination(1, 25)).path
      )
    }

    "show all teams label when no team filtering is present" in {
      val doc = view(reportFilters(summaryReport, SearchPagination(1, 25), Queues.allQueuesById))
      doc.getElementById("report_chosen_teams") should containText(messages("reporting.view_report.teams.all_teams"))
    }

    "show team names when report filters on selected teams" in {
      val teamsReport = summaryReport.copy(teams = Set("1", "3", "4"))

      val doc = view(reportFilters(teamsReport, SearchPagination(1, 25), Queues.allQueuesById))

      doc.getElementById("report_chosen_teams") should containText(
        messages("reporting.view_report.teams", "Gateway, CAP, CAR")
      )
    }

    "show correct link to change teams" in {
      val doc = view(reportFilters(summaryReport, SearchPagination(1, 25), Queues.allQueuesById))

      doc.getElementById("report_change_teams").child(0) should haveAttribute(
        "href",
        controllers.routes.ReportingController.showChangeTeamsFilter(summaryReport, SearchPagination(1, 25)).path
      )
    }
  }
}
