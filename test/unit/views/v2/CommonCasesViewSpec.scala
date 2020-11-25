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
import views.html.v2.my_cases_view

class myCasesViewSpec extends ViewSpec {

  val assignedToMeCasesTab =
    ApplicationTabViewModel(
      "message key", ApplicationsTab.assignedToMeCases(
        Seq(Cases.btiCaseExample, Cases.liabilityCaseExample)).applicationTabs
    )

  val referredByMeCasesTab =
    ApplicationTabViewModel(
      "referred by me heading", ApplicationsTab.referredByMe(
        Seq(Cases.btiCaseExample, Cases.liabilityCaseExample)).applicationTabs
    )

  def myCasesView: my_cases_view = injector.instanceOf[my_cases_view]

  "myCasesViewSpec" should {

    "render successfully" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("my-cases-tabs")
    }

    "contain my_cases_secondary_navigation" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("my-cases-sub-nav")
    }

    "contain my cases component" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      //doc should containElementWithID("correspondence_tab")
      //doc should containElementWithID("misc_tab")
    }


    "contain a heading" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("common-cases-heading")
    }

    "contain atar table" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("applicationTab.atar-table")
    }

    "contain liabilities table" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("applicationTab.liability-table")
    }

    "contain my_cases_secondary_navigation for Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("my-cases-sub-nav")
    }

    "contain my cases component for Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      //doc should containElementWithID("correspondence_tab")
      //doc should containElementWithID("misc_tab")
    }

    "contain a heading for referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("common-cases-heading")
    }

    "contain atar table in Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.atar-table")
    }

    "contain liabilities table in referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.liability-table")
    }

    //Uncomment the following tests when the components are implemented

    /*"contain liabilities correspondence" in {
      val doc = view(myCasesView("title", applicationsTab))

      doc should containElementWithID("correspondence")
    }

    "contain liabilities miscellaneous" in {
      val doc = view(myCasesView("title", applicationsTab))

      doc should containElementWithID("miscellaneous")
    }*/
  }

}
