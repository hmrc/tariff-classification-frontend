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

package views.components

import models.viewmodels._
import play.twirl.api.Html
import views.ViewMatchers.{containElementWithID, haveAttribute}
import views.ViewSpec
import views.html.components.open_cases_secondary_navigation

class OpenCasesSecondaryNavigationViewSpec extends ViewSpec {

  def openCasesSecondaryNavigationView(selectedTab: SubNavigationTab): Html =
    open_cases_secondary_navigation(selectedTab)

  "OpenCasesSecondaryNavigation" should {

    "display ATaR tab" in {
      val doc = view(openCasesSecondaryNavigationView(ATaRTab))

      doc should containElementWithID("sub_nav_atar_tab")

      val call = controllers.v2.routes.AllOpenCasesController.displayAllOpenCases(ATaRTab)
      doc.getElementById("sub_nav_atar_tab") should haveAttribute("href", call.url)

    }

    "display Liability tab" in {
      val doc = view(openCasesSecondaryNavigationView(LiabilitiesTab))

      doc should containElementWithID("sub_nav_liability_tab")

      val call = controllers.v2.routes.AllOpenCasesController.displayAllOpenCases(LiabilitiesTab)
      doc.getElementById("sub_nav_liability_tab") should haveAttribute("href", call.url)

    }

    "display Correspondence tab" in {
      val doc = view(openCasesSecondaryNavigationView(CorrespondenceTab))

      doc should containElementWithID("sub_nav_correspondence_tab")

      val call = controllers.v2.routes.AllOpenCasesController.displayAllOpenCases(CorrespondenceTab)
      doc.getElementById("sub_nav_correspondence_tab") should haveAttribute("href", call.url)

    }

    "display Miscellaneous tab" in {
      val doc = view(openCasesSecondaryNavigationView(MiscellaneousTab))

      doc should containElementWithID("sub_nav_miscellaneous_tab")

      val call = controllers.v2.routes.AllOpenCasesController.displayAllOpenCases(MiscellaneousTab)
      doc.getElementById("sub_nav_miscellaneous_tab") should haveAttribute("href", call.url)

    }

  }
}
