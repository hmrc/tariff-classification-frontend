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

package views.v2

import utils.Cases
import views.ViewMatchers._
import views.ViewSpec
import views.html.v2.case_status_tab

class CaseStatusTabViewSpec extends ViewSpec {

  "Case Status" should {

    "render the case status" in {

      val doc = view(case_status_tab(Cases.btiCaseExample, "id"))


      doc.text()                      shouldBe "case status OPEN"
      doc.getElementById("id-status") should haveClass("govuk-tag--blue")
    }

    "render the live liability case status" in {

      val doc = view(case_status_tab(Cases.liabilityLiveCaseExample, "id"))


      doc.text() should include("OPEN")
      doc.text() shouldNot include("OVERDUE")
      doc.getElementById("id-status") should haveClass("govuk-tag--blue")
    }

    "render the overdue live liability case status" in {

      val doc = view(case_status_tab(Cases.liabilityLiveCaseExample.copy(daysElapsed = 7), "id"))


      doc.text()                       should include("OPEN")
      doc.text()                       should include("OVERDUE")
      doc.getElementById("id-overdue") should haveClass("govuk-tag--red")
      doc.getElementById("id-status")  should haveClass("govuk-tag--blue")
    }

  }
}
