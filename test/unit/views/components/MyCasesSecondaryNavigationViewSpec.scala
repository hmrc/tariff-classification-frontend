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

package views.components
import models.viewmodels.{AssignedToMeTab, ReferredByMeTab, CompletedByMeTab}
import views.ViewMatchers.{containElementWithID, haveAttribute}
import views.ViewSpec
import views.html.components.my_cases_secondary_navigation

class MyCasesSecondaryNavigationViewSpec extends ViewSpec {

  val myCasesSecondaryView: my_cases_secondary_navigation = injector.instanceOf[my_cases_secondary_navigation]

  "MyCasesSecondaryNavigation" should {

    "display AssignedToMe tab" in {
      val doc = view(myCasesSecondaryView(AssignedToMeTab))

      doc should containElementWithID("sub_nav_assigned_to_me_tab")

      val call = controllers.v2.routes.MyCasesController.displayMyCases(AssignedToMeTab)
      doc.getElementById("sub_nav_assigned_to_me_tab") should haveAttribute("href", call.url)

    }

    "display ReferredByMe tab" in {
      val doc = view(myCasesSecondaryView(ReferredByMeTab))

      doc should containElementWithID("sub_nav_referred_by_me_tab")

      val call = controllers.v2.routes.MyCasesController.displayMyCases(ReferredByMeTab)
      doc.getElementById("sub_nav_referred_by_me_tab") should haveAttribute("href", call.url)

    }

    "display CompletedByMe tab" in {
      val doc = view(myCasesSecondaryView(CompletedByMeTab))

      doc should containElementWithID("sub_nav_completed_by_me_tab")

      val call = controllers.v2.routes.MyCasesController.displayMyCases(CompletedByMeTab)
      doc.getElementById("sub_nav_completed_by_me_tab") should haveAttribute("href", call.url)

    }

  }
}
