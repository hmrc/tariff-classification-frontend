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

package models.viewmodels.managementtools

import models._

case class UsersTab(tabMessageKey: String, elementId: String, searchResult: Paged[User])

case class UsersTabViewModel(
  headingMessageKey: String,
  usersByQueueTabs: List[UsersTab],
  assignedUsersTab: UsersTab,
  unassignedUsersTab: UsersTab,
  readOnlyTab: UsersTab
)

object UsersTabViewModel {
  def forManagedTeams(managedTeams: List[Queue]) = {
    //todo replace stubs with queries
    //create constants for roles
    val assignedUsers = UsersTab("Assigned", "assigned-tab", Paged(Users.allUsers.filter(u => u.isAssigned)))
    val unassignedUsers = UsersTab(
      "Unassigned",
      "unassigned-tab",
      Paged(Users.allUsers.filter(u => !u.isAssigned && u.role != "Read only"))
    )
    val readonlyUsers = UsersTab("Read only", "readonly-tab", Paged(Users.allUsers.filter(u => u.role == "Read only")))
    val teamUsers = managedTeams.map { managedTeam =>
      UsersTab(
        managedTeam.slug.toUpperCase,
        s"${managedTeam.slug.toUpperCase}-tab",
        Paged(Users.allUsers.filter(u => u.teams.contains(managedTeam)))
      )
    }

    UsersTabViewModel(
      "management.manage-users.heading",
      teamUsers,
      assignedUsers,
      unassignedUsers,
      readonlyUsers
    )

  }
}
