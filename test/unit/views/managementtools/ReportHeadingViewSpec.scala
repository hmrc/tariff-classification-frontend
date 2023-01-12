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

import models._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.reportHeading

class ReportHeadingViewSpec extends ViewSpec {
  "reportHeading view" should {
    "render heading text" in {
      val doc = view(reportHeading("Case count by status", "Report results"))
      doc                                                should containElementWithClass("govuk-heading-xl")
      doc.getElementsByClass("govuk-heading-xl").first() should containText("Report results")
    }

    "render report name caption" in {
      val doc = view(reportHeading("Case count by status", "Report results"))
      doc                                  should containElementWithID("report-caption")
      doc.getElementById("report-caption") should containText("Case count by status")
    }
  }
}
