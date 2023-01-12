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

package models.viewmodels.managementtools

import models._

case class UsersTab(tabMessageKey: String, elementId: String, searchResult: List[Operator])

case class UsersTabViewModel(
  headingMessageKey: String,
  usersByQueueTabs: List[UsersTab],
  assignedUsersTab: UsersTab,
  unassignedUsersTab: UsersTab
)

object UsersTabViewModel {

  def fromUsers(manager: Operator, users: Paged[Operator]): UsersTabViewModel = {

    val managedTeamsTabs = manager.memberOfTeams.map { managedTeam =>
      val userForTeams = users.results.filter(_.memberOfTeams.contains(managedTeam))
      val messageKey   = Queues.queueById(managedTeam).map(_.name).getOrElse("unknown")

      UsersTab(tabMessageKey = messageKey, elementId = messageKey, searchResult = userForTeams.toList)
    }.toList

    val (assignedUsers, unassignedUser) = users.results.partition(_.memberOfTeams.nonEmpty)

    val assignedUsersTab   = UsersTab("assigned", "assigned_tab", assignedUsers.toList)
    val unassignedUsersTab = UsersTab("unassigned", "unassigned_tab", unassignedUser.toList)

    UsersTabViewModel(
      "Manage users",
      managedTeamsTabs,
      assignedUsersTab,
      unassignedUsersTab
    )
  }
}
