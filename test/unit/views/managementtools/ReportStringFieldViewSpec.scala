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

package views.managementtools

import models._
import models.reporting._
import play.twirl.api.{Html, StringInterpolation}
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.reportStringField

class ReportStringFieldViewSpec extends ViewSpec {
  // Jsoup can't parse an isolated <td> tag
  def withTableWrapper(viewHtml: Html) =
    html"<table>$viewHtml</table>"

  "reportStringField view" should {

    "render with the specified ID" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.Chapter, StringResultField(ReportField.Chapter.fieldName, Some("85")), Map.empty, Map.empty, "case-report", 0)))
      doc should containElementWithID("case-report-chapter-0")
    }

    "render reference link for a case reference" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.Reference, StringResultField(ReportField.Reference.fieldName, Some("123456")), Map.empty, Map.empty, "case-report", 1)))
      doc should containElementWithID("case-report-reference-1")
      doc.getElementById("case-report-reference-1").child(0).attr("href").trim shouldBe controllers.routes.CaseController.get("123456").path
    }

    "render Unknown when there is no case reference" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.Reference, StringResultField(ReportField.Reference.fieldName, None), Map.empty, Map.empty, "case-report", 1)))
      doc should containElementWithID("case-report-reference-1")
      doc.getElementById("case-report-reference-1") should containText("Unknown")
    }

    "render team name for a known team ID" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.Team, StringResultField(ReportField.Team.fieldName, Some("1")), Map.empty, Queues.allQueuesById, "case-report", 0)))
      doc should containElementWithID("case-report-assigned_team-0")
      doc.getElementById("case-report-assigned_team-0") should containText("Gateway")
    }

    "render Unknown for an unknown team ID" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.Team, StringResultField(ReportField.Team.fieldName, Some("24")), Map.empty, Queues.allQueuesById, "case-report", 0)))
      doc should containElementWithID("case-report-assigned_team-0")
      doc.getElementById("case-report-assigned_team-0") should containText("Unknown")
    }

    "render Gateway when there is no assigned team" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.Team, StringResultField(ReportField.Team.fieldName, None), Map.empty, Queues.allQueuesById, "case-report", 0)))
      doc should containElementWithID("case-report-assigned_team-0")
      doc.getElementById("case-report-assigned_team-0") should containText("Gateway")
    }

    "render user name for a known user" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.User, StringResultField(ReportField.User.fieldName, Some("1")), Map("1" -> Operator("1", Some("Gandalf"))), Map.empty, "case-report", 0)))
      doc should containElementWithID("case-report-assigned_user-0")
      doc.getElementById("case-report-assigned_user-0") should containText("Gandalf")
    }

    "render PID for an unknown user" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.User, StringResultField(ReportField.User.fieldName, Some("1")), Map.empty, Map.empty, "case-report", 0)))
      doc should containElementWithID("case-report-assigned_user-0")
      doc.getElementById("case-report-assigned_user-0") should containText("PID 1")
    }

    "render Unassigned when there is no assigned user" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.User, StringResultField(ReportField.User.fieldName, None), Map("1" -> Operator("1", Some("Gandalf"))), Map.empty, "case-report", 0)))
      doc should containElementWithID("case-report-assigned_user-0")
      doc.getElementById("case-report-assigned_user-0") should containText("Unassigned")
    }

    "render Unknown for any other missing data" in {
      val doc = view(withTableWrapper(reportStringField(ReportField.Chapter, StringResultField(ReportField.Reference.fieldName, None), Map.empty, Map.empty, "case-report", 1)))
      doc should containElementWithID("case-report-chapter-1")
      doc.getElementById("case-report-chapter-1") should containText("Unknown")
    }
  }
}
