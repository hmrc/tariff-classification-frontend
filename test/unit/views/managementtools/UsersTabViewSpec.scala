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

package views.managementtools

import models.viewmodels.managementtools.{UsersTab, UsersTabViewModel}
import models.{Case, Operator, Role}
import play.twirl.api.HtmlFormat
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.users_tab

class UsersTabViewSpec extends ViewSpec {

  val users: List[Operator] = List(
    Operator("id1", Some("operator 1"), Some("email@operator.com"), Role.CLASSIFICATION_OFFICER, Seq("2")),
    Operator("id2", Some("operator 2"), Some("email@operator.com"), Role.CLASSIFICATION_OFFICER, Seq("2", "3", "4")),
  )

  val unassignedUsersList: List[Operator] = List(
    Operator("id3", Some("operator 3"), Some("email@operator.com"), Role.CLASSIFICATION_OFFICER, Seq.empty),
    Operator("id4", Some("operator 4"), Some("email@operator.com"), Role.CLASSIFICATION_OFFICER, Seq.empty),
  )

  val usersByQueue: UsersTab = UsersTab(
    "act",
    "act_tab",
    users
  )

  val assignedUsers: UsersTab = UsersTab(
    "assigned",
    "assigned_tab",
    users
  )

  val unassignedUsers: UsersTab = UsersTab(
    "unassigned",
    "unassigned_tab",
    unassignedUsersList
  )

  val usersTabViewModel = UsersTabViewModel(
    "Manage users",
    List(usersByQueue),
    assignedUsers,
    unassignedUsers
  )

  val emptyUsersTabViewModel = UsersTabViewModel(
    "Manage users",
    List(usersByQueue.copy(searchResult = List.empty)),
    assignedUsers.copy(searchResult   = List.empty),
    unassignedUsers.copy(searchResult = List.empty)
  )

  val count: Map[String, List[Case]] = Map("id1" -> List(aCase(), aCase()), "id2" -> List(aCase(), aCase()))

  def usersTabView(
    usersTabViewModel: UsersTabViewModel = usersTabViewModel,
    caseCount: Map[String, List[Case]]   = count): HtmlFormat.Appendable =
    users_tab(usersTabViewModel, caseCount)

  "usersTabView" should {

    "include users by team table with correct columns" in {

      val doc = view(usersTabView())

      doc should containElementWithID("act_tab")
      doc.getElementById("act_tab") should containText(
        messages("management.manage-users.tabSubHeading", usersByQueue.tabMessageKey))
    }

    "include assigned users table with correct columns" in {

      val doc = view(usersTabView())

      doc should containElementWithID("assigned_tab")
      doc.getElementById("assigned_tab") should containText(
        messages("management.manage-users.tabSubHeading", assignedUsers.tabMessageKey))

    }

    "include unassigned users table with correct columns" in {

      val doc = view(usersTabView())

      doc should containElementWithID("unassigned_tab")
      doc.getElementById("unassigned_tab") should containText(
        messages("management.manage-users.tabSubHeading", unassignedUsers.tabMessageKey))

    }

    "display correct message when there are no users in users by team table" in {

      val doc = view(usersTabView(emptyUsersTabViewModel))

      doc should containElementWithID("act_tab")
      doc.getElementById("act_tab") should containText("No users in this team")
    }

    "display correct message when there are no users in assigned users table" in {

      val doc = view(usersTabView(emptyUsersTabViewModel))

      doc should containElementWithID("assigned_tab")
      doc.getElementById("assigned_tab") should containText("No users in this team")
    }

    "display correct message when there are no users in unassigned users table" in {

      val doc = view(usersTabView(emptyUsersTabViewModel))

      doc should containElementWithID("unassigned_tab")
      doc.getElementById("unassigned_tab") should containText("No users in this team")
    }
    /*
    for ((user, index) <- usersTab.searchResult.zipWithIndex) {
      s"populate assigned users table with user id: ${user.id}" in {

        val doc = view(usersTabView())

        doc                                         should containElementWithID(s"assigned-details-$index")
        doc.getElementById(s"assigned-user-$index") should containText(user.name.getOrElse("unknown"))
        doc.getElementById(s"assigned-role-$index") should containText(Role.format(user.role))
        doc.getElementById(s"assigned-team-$index") should containText(user.getMemberTeamNames.mkString(", "))
        doc.getElementById(s"assigned-case-$index") should containText(
          count.get(user.id).getOrElse(List.empty).size.toString)
      }
    }
   */
  }
}
