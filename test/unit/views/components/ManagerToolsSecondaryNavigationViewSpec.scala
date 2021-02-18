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
import models.viewmodels._
import play.twirl.api.Html
import views.ViewMatchers.{containElementWithID, haveAttribute}
import views.ViewSpec
import views.html.components.{manager_tools_secondary_navigation, open_cases_secondary_navigation}

class ManagerToolsSecondaryNavigationViewSpec extends ViewSpec {

  def managerToolsSecondaryNavigationView(selectedTab: SubNavigationTab): Html =
    manager_tools_secondary_navigation(selectedTab)

  "ManagerToolsSecondaryNavigation" should {

    "display Users tab" in {
      val doc = view(managerToolsSecondaryNavigationView(ManagerToolsUsersTab))

      doc should containElementWithID("sub_nav_manager_tools_users_tab")

      val call = controllers.v2.routes.ManageUsersController.displayManageUsers()
      doc.getElementById("sub_nav_manager_tools_users_tab") should haveAttribute("href", call.url)

    }

    "display Teams tab" in {
      val doc = view(managerToolsSecondaryNavigationView(ManagerToolsTeamsTab))

      doc should containElementWithID("sub_nav_manager_tools_teams_tab")

      val call = controllers.v2.routes.ManagerToolsController.displayManagerTools(ManagerToolsTeamsTab)
      doc.getElementById("sub_nav_manager_tools_teams_tab") should haveAttribute("href", call.url)

    }

    "display Keywords tab" in {
      val doc = view(managerToolsSecondaryNavigationView(ManagerToolsKeywordsTab))

      doc should containElementWithID("sub_nav_manager_tools_keywords_tab")

      val call = controllers.v2.routes.ManageKeywordsController.displayManageKeywords()
      doc.getElementById("sub_nav_manager_tools_keywords_tab") should haveAttribute("href", call.url)

    }

    "display Reports tab" in {
      val doc = view(managerToolsSecondaryNavigationView(ManagerToolsReportsTab))

      doc should containElementWithID("sub_nav_manager_tools_reports_tab")

      val call = controllers.v2.routes.ManagerToolsController.displayManagerTools(ManagerToolsReportsTab)
      doc.getElementById("sub_nav_manager_tools_reports_tab") should haveAttribute("href", call.url)

    }

  }
}
