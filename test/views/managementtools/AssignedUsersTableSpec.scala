/*
 * Copyright 2025 HM Revenue & Customs
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
import views.html.managementtools.assignedUsersTable

class AssignedUsersTableSpec extends ViewSpec {

  val users: List[Operator] = List(
    Operator("id1", Some("operator 1"), Some("email@operator.com"), Role.CLASSIFICATION_OFFICER, Seq("2")),
    Operator("id2", Some("operator 2"), Some("email@operator.com"), Role.CLASSIFICATION_OFFICER, Seq("2", "3", "4"))
  )
  val usersTab: UsersTab = UsersTab(
    "assigned",
    "assigned_tab",
    users
  )

  val count: Map[String, List[Case]] = Map("id1" -> List(aCase(), aCase()), "id2" -> List(aCase(), aCase()))

  def assignedUsersTableView(
    usersTab: UsersTab = usersTab,
    caseCount: Map[String, List[Case]] = count
  ): HtmlFormat.Appendable =
    assignedUsersTable(usersTab, caseCount)

  "assignedUsersTable" should {

    "include assigned users table with correct columns" in {

      val doc = view(assignedUsersTableView())

      doc                                 should containElementWithID("assigned-table")
      doc                                 should containElementWithID("assigned-details")
      doc.getElementById("assigned-user") should containText("User")
      doc.getElementById("assigned-role") should containText("Role")
      doc.getElementById("assigned-team") should containText("Team")
      doc.getElementById("assigned-case") should containText("Case")
    }

    for ((user, index) <- usersTab.searchResult.zipWithIndex)
      s"populate assigned users table with user id: ${user.id}" in {

        val doc = view(assignedUsersTableView())

        doc                                         should containElementWithID(s"assigned-details-$index")
        doc.getElementById(s"assigned-user-$index") should containText(user.name.getOrElse("unknown"))
        doc.getElementById(s"assigned-role-$index") should containText(Role.format(user.role))
        doc.getElementById(s"assigned-team-$index") should containText(user.getMemberTeamNames.mkString(", "))
        doc.getElementById(s"assigned-case-$index") should containText(
          count.getOrElse(user.id, List.empty).size.toString
        )
      }
  }
}
