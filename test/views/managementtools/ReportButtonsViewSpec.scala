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
import controllers.routes
import models.reporting._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.reportButtons

class ReportButtonsViewSpec extends ViewSpec {
  "reportButtons view" should {
    val summaryReport =
      SummaryReport("Summary report", groupBy = NonEmptySeq.one(ReportField.Status), sortBy = ReportField.Status)
    val caseReport =
      CaseReport(
        "Case report",
        fields = NonEmptySeq.of(ReportField.Reference, ReportField.Status, ReportField.ElapsedDays)
      )

    "render with the specified ID" in {
      val doc = view(reportButtons(summaryReport, "test-report"))
      doc should containElementWithID("test-report-buttons")
      doc should containElementWithID("test-report-print-button")
      doc should containElementWithID("test-report-download-button")
    }

    "display the appropriate download link for a summary report" in {
      val doc = view(reportButtons(summaryReport, "test-report"))
      doc.getElementById("test-report-download-button").attr("href").trim shouldBe routes.ReportingController
        .downloadSummaryReport(summaryReport)
        .path
    }

    "display the appropriate download link for a case report" in {
      val doc = view(reportButtons(caseReport, "test-report"))
      doc.getElementById("test-report-download-button").attr("href").trim shouldBe routes.ReportingController
        .downloadCaseReport(caseReport)
        .path
    }
  }
}
