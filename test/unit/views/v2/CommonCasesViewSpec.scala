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

import models.viewmodels.{ApplicationTabViewModel, ApplicationsTab}
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.v2.common_cases_view

class CommonCasesViewSpec extends ViewSpec {

  val applicationsTab =
    ApplicationTabViewModel(
      "message key", ApplicationsTab.assignedToMeCases(Seq(Cases.btiCaseExample, Cases.liabilityCaseExample)).applicationTabs
    )

  def commonCasesView: common_cases_view = injector.instanceOf[common_cases_view]

  "CommonCasesViewSpec" should {

    "render successfully" in {
      val doc = view(commonCasesView(applicationsTab))

      doc should containElementWithID("my-cases-tabs")
    }

    "contain my_cases_secondary_navigation" in {
      val doc = view(commonCasesView(applicationsTab))

      doc should containElementWithID("my-cases-sub-nav")
    }

    "contain my cases component" in {
      val doc = view(commonCasesView(applicationsTab))

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      //doc should containElementWithID("correspondence_tab")
      //doc should containElementWithID("misc_tab")
    }

    "contain a heading" in {
      val doc = view(commonCasesView(applicationsTab))

      doc should containElementWithID("common-cases-heading")
    }

    "contain atar table" in {
      val doc = view(commonCasesView(applicationsTab))

      doc should containElementWithID("applicationTab.assignedToMe.atar-table")
    }

    "contain liabilities table" in {
      val doc = view(commonCasesView(applicationsTab))

      doc should containElementWithID("applicationTab.assignedToMe.liability-table")
    }

    //Uncomment the following tests when the components are implemented

    /*"contain liabilities correspondence" in {
      val doc = view(commonCasesView("title", applicationsTab))

      doc should containElementWithID("correspondence")
    }

    "contain liabilities miscellaneous" in {
      val doc = view(commonCasesView("title", applicationsTab))

      doc should containElementWithID("miscellaneous")
    }*/
  }

}
