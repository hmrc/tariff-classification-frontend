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

package models.viewmodels.managementtools

import models.reporting.Report

case class ReportTabRow(nameMessageKey: String, reportId: String)

case class ReportTab(tabMessageKey: String, elementId: String, reports: List[ReportTabRow])

case class ReportingTabViewModel(
  headingMessageKey: String,
  summaryReportTab: ReportTab,
  statusReportTab: ReportTab,
  atarReportTab: ReportTab,
  liabilityReportTab: ReportTab,
  correspondenceReportTab: ReportTab,
  miscReportTab: ReportTab
)

object ReportingTabViewModel {
  def reportImplemented(row: ReportTabRow) =
    Report.byId.contains(row.reportId)

  def summaryTabRows: List[ReportTabRow] = List(
    ReportTabRow("number-of-new-cases", "number-of-new-cases"),
    ReportTabRow("new-and-open-cases", "new-and-open-cases"),
    ReportTabRow("number-of-cases-in-teams", "number-of-cases-in-teams"),
    ReportTabRow("number-of-cases-per-user", "number-of-cases-per-user"),
    ReportTabRow("working-days-liability-cases", "working-days-liability-cases"),
    ReportTabRow("calendar-days-atar-cases", "calendar-days-atar-cases"),
    ReportTabRow("rejection-breakdown", "rejection-breakdown"),
    ReportTabRow("atar-summary", "atar-summary"),
    ReportTabRow("liabilities-summary", "liabilities-summary")
  ).filter(reportImplemented)

  def statusTabRows: List[ReportTabRow] = List(
    ReportTabRow("case-status", "case-status"),
    ReportTabRow("suppressed-cases", "suppressed-cases"),
    ReportTabRow("open-cases", "open-cases"),
    ReportTabRow("number-of-open-cases", "number-of-open-cases"),
    ReportTabRow("referred-cases", "referred-cases"),
    ReportTabRow("completed-cases", "completed-cases"),
    ReportTabRow("under-review-cases-by-chapter", "under-review-cases-by-chapter"),
    ReportTabRow("under-review-cases-by-assigned-user", "under-review-cases-by-assigned-user"),
    ReportTabRow("under-appeal-cases-by-chapter", "under-appeal-cases-by-chapter"),
    ReportTabRow("under-appeal-cases-by-assigned-user", "under-appeal-cases-by-assigned-user"),
    ReportTabRow("cancelled-cases-by-chapter", "cancelled-cases-by-chapter"),
    ReportTabRow("cancelled-cases-by-assigned-user", "cancelled-cases-by-assigned-user")
  ).filter(reportImplemented)

  def atarTabRows: List[ReportTabRow] = List(
    ReportTabRow("new-atar-cases", "new-atar-cases"),
    ReportTabRow("atar-summary", "atar-summary"),
    ReportTabRow("calendar-days-atar-cases", "calendar-days-atar-cases")
  ).filter(reportImplemented)

  def liabilityTabRows: List[ReportTabRow] = List(
    ReportTabRow("liabilities-cases", "liabilities-cases"),
    ReportTabRow("new-liabilities-cases-non-live", "new-liabilities-cases-non-live"),
    ReportTabRow("new-liabilities-cases", "new-liabilities-cases-live"),
    ReportTabRow("new-liabilities", "new-liabilities")
  ).filter(reportImplemented)

  def correspondenceTabRows: List[ReportTabRow] = List(
    ReportTabRow("correspondence-cases", "correspondence-cases")
  ).filter(reportImplemented)

  def miscTabRows: List[ReportTabRow] = List(
    ReportTabRow("miscellaneous-cases", "miscellaneous-cases")
  ).filter(reportImplemented)

  def reportingTabs(): ReportingTabViewModel =
    ReportingTabViewModel(
      "Reporting dashboard",
      summaryReportTab        = ReportTab("summary", "summary_report_tab", summaryTabRows),
      statusReportTab         = ReportTab("status", "status_report_tab", statusTabRows),
      atarReportTab           = ReportTab("atar", "atar_report_tab", atarTabRows),
      liabilityReportTab      = ReportTab("liability", "liability_report_tab", liabilityTabRows),
      correspondenceReportTab = ReportTab("correspondence", "correspondence_report_tab", correspondenceTabRows),
      miscReportTab           = ReportTab("miscellaneous", "misc_report_tab", miscTabRows)
    )
}
