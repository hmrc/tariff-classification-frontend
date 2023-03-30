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

import models.viewmodels.managementtools.UsersTab
import models.{Case, Operator, Role}
import play.twirl.api.HtmlFormat
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.usersByTeamTable

class UsersByTeamTableSpec extends ViewSpec {

  val users: List[Operator] = List(
    Operator("id1", Some("operator 1"), Some("email@operator.com"), Role.CLASSIFICATION_OFFICER, Seq("2")),
    Operator("id2", Some("operator 2"), Some("email@operator.com"), Role.CLASSIFICATION_OFFICER, Seq("2", "3", "4"))
  )

  val usersTab: UsersTab = UsersTab(
    "act",
    "act_tab",
    users
  )

  val count: Map[String, List[Case]] = Map("id1" -> List(aCase(), aCase()), "id2" -> List(aCase(), aCase()))

  def assignedUsersTableView(
    usersTab: UsersTab                 = usersTab,
    caseCount: Map[String, List[Case]] = count
  ): HtmlFormat.Appendable =
    usersByTeamTable(usersTab, caseCount)

  "usersByTeamTable" should {

    "include users by team table with correct columns" in {

      val doc = view(assignedUsersTableView())

      doc                            should containElementWithID("act-table")
      doc                            should containElementWithID("act-details")
      doc.getElementById("act-user") should containText("User")
      doc.getElementById("act-role") should containText("Role")
      doc.getElementById("act-case") should containText("Case")
    }

    for ((user, index) <- usersTab.searchResult.zipWithIndex) {
      s"populate users by team table with user id: ${user.id}" in {

        val doc = view(assignedUsersTableView())

        doc                                    should containElementWithID(s"act-details-$index")
        doc.getElementById(s"act-user-$index") should containText(user.name.getOrElse("unknown"))
        doc.getElementById(s"act-role-$index") should containText(Role.format(user.role))
        doc.getElementById(s"act-case-$index") should containText(
          count.getOrElse(user.id, List.empty).size.toString
        )
      }
    }
  }
}
