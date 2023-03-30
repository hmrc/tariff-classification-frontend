/*
 * Copyright 2023 HM Revenue & Customs
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

import models.viewmodels.managementtools._
import play.twirl.api.HtmlFormat
import views.ViewMatchers.containText
import views.ViewSpec
import views.html.managementtools.reports_tab

class ReportsTabSpec extends ViewSpec {

  val reportingTabViewModel: ReportingTabViewModel = ReportingTabViewModel.reportingTabs()

  def manageReportsView: HtmlFormat.Appendable = reports_tab(reportingTabViewModel)

  "ReportsTab page" should {

    "include a heading and tabs" in {
      val doc = view(manageReportsView)

      doc.getElementById("common-cases-heading") should containText(messages("reporting.manage_reports.heading"))
      doc.getElementById("manage-keywords-tabs") should containText(messages("reporting.summary.tab.heading"))
      doc.getElementById("manage-keywords-tabs") should containText(messages("reporting.status.tab.heading"))
      doc.getElementById("manage-keywords-tabs") should containText(messages("reporting.atar.tab.heading"))
      doc.getElementById("manage-keywords-tabs") should containText(messages("reporting.liability.tab.heading"))
      doc.getElementById("manage-keywords-tabs") should containText(messages("reporting.correspondence.tab.heading"))
      doc.getElementById("manage-keywords-tabs") should containText(messages("reporting.miscellaneous.tab.heading"))
    }

    "tabs should have a heading" in {
      val doc = view(manageReportsView)

      doc.getElementById(reportingTabViewModel.summaryReportTab.elementId) should containText(
        messages("reporting.summary.heading")
      )
      doc.getElementById(reportingTabViewModel.statusReportTab.elementId) should containText(
        messages("reporting.status.heading")
      )
      doc.getElementById(reportingTabViewModel.atarReportTab.elementId) should containText(
        messages("reporting.atar.heading")
      )
      doc.getElementById(reportingTabViewModel.liabilityReportTab.elementId) should containText(
        messages("reporting.liability.heading")
      )
      doc.getElementById(reportingTabViewModel.correspondenceReportTab.elementId) should containText(
        messages("reporting.correspondence.heading")
      )
      doc.getElementById(reportingTabViewModel.miscReportTab.elementId) should containText(
        messages("reporting.miscellaneous.heading")
      )
    }
  }
}
