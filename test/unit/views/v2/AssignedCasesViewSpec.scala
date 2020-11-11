/*
 * Copyright 2020 HM Revenue & Customs
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

package views.v2

import views.ViewMatchers.containElementWithID
import views.html.v2.assigned_cases_v2
import views.ViewSpec

class AssignedCasesViewSpec extends ViewSpec {

  def assignedCases: assigned_cases_v2 = injector.instanceOf[assigned_cases_v2]

  "AssignedCasesViewSpec" should {

    "render successfully" in {
      val doc = view(assignedCases())

      doc should containElementWithID("liability_tabs")
    }

    "contain an atar, liability, correspondence and misc tabs" in {
      val doc = view(assignedCases())

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      doc should containElementWithID("correspondence_tab")
      doc should containElementWithID("misc_tab")
    }

    "contain a heading" in {
      val doc = view(assignedCases())

      doc should containElementWithID("case-heading")
    }
  }

}
