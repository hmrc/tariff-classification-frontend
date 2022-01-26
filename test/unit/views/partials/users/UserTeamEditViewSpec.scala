/*
 * Copyright 2022 HM Revenue & Customs
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

package views.partials.users

import models.forms.v2.UserEditTeamForm
import models.viewmodels.ManagerToolsUsersTab
import models.{Operator, Role}
import views.ViewMatchers.{containElementWithID, containElementWithTag}
import views.ViewSpec
import views.html.partials.users.user_team_edit

class UserTeamEditViewSpec extends ViewSpec {

  def userTeamEdit: user_team_edit = injector.instanceOf[user_team_edit]
  private val userEditForm         = UserEditTeamForm.editTeamsForm

  private val operator = Operator(
    id            = "PID5",
    name          = Some("John Doe"),
    email         = Some("john@doe.o"),
    role          = Role.CLASSIFICATION_OFFICER,
    memberOfTeams = Seq("1", "5")
  )

  "User Team Edit" should {
    "render successfully" in {
      val doc = view(userTeamEdit(operator, userEditForm))

      doc should containElementWithID("user-team-edit-heading")
      doc should containElementWithID("cancel_edit_user_teams-button")
    }
  }
}
