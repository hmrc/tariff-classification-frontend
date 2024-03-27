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

import models.forms._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.reportDateFilter

class ReportDateFilterViewSpec extends ViewSpec {
  "reportDateFilter view" should {
    val form = ReportDateForm.form

    "render min and max date fields with the correct IDs" in {
      val doc = view(reportDateFilter(form))
      doc should containElementWithID("dateRange_min")
      doc should containElementWithID("dateRange_min_year")
      doc should containElementWithID("dateRange_min_month")
      doc should containElementWithID("dateRange_min_day")

      doc should containElementWithID("dateRange_max")
      doc should containElementWithID("dateRange_max_year")
      doc should containElementWithID("dateRange_max_month")
      doc should containElementWithID("dateRange_max_day")
    }

    "show label" in {
      val doc = view(reportDateFilter(form))
      doc.getElementById("dateRange_min") should containText(messages("reporting.choose_date.start_date"))
      doc.getElementById("dateRange_max") should containText(messages("reporting.choose_date.end_date"))
    }

    "show hint text" in {
      val doc = view(reportDateFilter(form))
      doc.getElementById("dateRange_min") should containText(messages("reporting.choose_date.example_date"))
      doc.getElementById("dateRange_max") should containText(messages("reporting.choose_date.example_date"))
    }
  }
}
