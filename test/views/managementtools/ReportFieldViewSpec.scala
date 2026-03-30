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

import models._
import models.reporting._
import play.twirl.api.{Html, StringInterpolation}
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.reportField

import java.time.Instant

class ReportFieldViewSpec extends ViewSpec {
  // Jsoup can't parse an isolated <td> tag
  def withTableWrapper(viewHtml: Html) =
    html"<table>$viewHtml</table>"

  "reportField view" should {
    "render with the specified ID" in {
      val doc = view(
        withTableWrapper(
          reportField(
            ReportField.Chapter,
            StringResultField(ReportField.Chapter.fieldName, Some("85")),
            Map.empty,
            Map.empty,
            "case-report",
            0
          )
        )
      )
      doc should containElementWithID("case-report-chapter-0")
    }

    "render date fields correctly" in {
      val doc = view(
        withTableWrapper(
          reportField(
            ReportField.DateCreated,
            DateResultField(ReportField.DateCreated.fieldName, Some(Instant.parse("2019-04-24T09:00:00.00Z"))),
            Map.empty,
            Map.empty,
            "case-report",
            0
          )
        )
      )
      doc                                              should containElementWithID("case-report-date_created-0")
      doc.getElementById("case-report-date_created-0") should containText("24 Apr 2019")
    }

    "render case type fields correctly" in {
      val doc = view(
        withTableWrapper(
          reportField(
            ReportField.CaseType,
            CaseTypeResultField(ReportField.CaseType.fieldName, Some(ApplicationType.ATAR)),
            Map.empty,
            Map.empty,
            "case-report",
            0
          )
        )
      )
      doc                                           should containElementWithID("case-report-case_type-0")
      doc.getElementById("case-report-case_type-0") should containText("ATaR")
    }

    "render status fields correctly" in {
      val doc = view(
        withTableWrapper(
          reportField(
            ReportField.Status,
            StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.CANCELLED)),
            Map.empty,
            Map.empty,
            "case-report",
            0
          )
        )
      )
      doc                                        should containElementWithID("case-report-status-0")
      doc.getElementById("case-report-status-0") should containText("CANCELLED")
    }
    "render total days field correctly" in {
      val doc = view(
        withTableWrapper(
          reportField(
            ReportField.TotalDays,
            NumberResultField(ReportField.TotalDays.fieldName, Some(120)),
            Map.empty,
            Map.empty,
            "case-report",
            0
          )
        )
      )
      doc                                            should containElementWithID("case-report-total_days-0")
      doc.getElementById("case-report-total_days-0") should containText("120")
      doc.getElementById("case-report-total_days-0").hasClass("live-red-text")
    }
  }
}
