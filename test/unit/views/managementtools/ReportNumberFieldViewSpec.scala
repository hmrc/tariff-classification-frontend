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
import views.html.managementtools.reportNumberField

class ReportNumberFieldViewSpec extends ViewSpec {
  // Jsoup can't parse an isolated <td> tag
  def withTableWrapper(viewHtml: Html) =
    html"<table>$viewHtml</table>"

  "reportNumberField view" should {

    "render with the specified ID" in {
      val doc = view(withTableWrapper(reportNumberField(NumberResultField(ReportField.ElapsedDays.fieldName, Some(12)), "case-report", 0)))
      doc should containElementWithID("case-report-elapsed_days-0")
    }

    "render the numeric value retrieved" in {
      val doc = view(withTableWrapper(reportNumberField(NumberResultField(ReportField.ElapsedDays.fieldName, Some(12)), "case-report", 0)))
      doc should containElementWithID("case-report-elapsed_days-0")
      doc.getElementById("case-report-elapsed_days-0") should containText("12")
    }

    "render 0 if no value was found" in {
      val doc = view(withTableWrapper(reportNumberField(NumberResultField(ReportField.ElapsedDays.fieldName, None), "case-report", 0)))
      doc should containElementWithID("case-report-elapsed_days-0")
      doc.getElementById("case-report-elapsed_days-0") should containText("0")
    }
  }
}
