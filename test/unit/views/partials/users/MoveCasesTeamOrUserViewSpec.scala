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

package views.partials.users

import models.forms.v2.TeamOrUserForm
import models.viewmodels.ManagerToolsUsersTab
import models.{Operator, Role}
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.users.move_cases_team_or_user

class MoveCasesTeamOrUserViewSpec extends ViewSpec {

  def teamOrUser: move_cases_team_or_user = injector.instanceOf[move_cases_team_or_user]
  private val teamOrUserForm              = TeamOrUserForm.form

  "Team or User" should {
    "render successfully with one case" in {
      val doc = view(teamOrUser(1, teamOrUserForm))

      doc should containText(messages("users.move_cases.team_or_user.header.single"))
    }

    "render successfully with multiple cases" in {
      val doc = view(teamOrUser(5, teamOrUserForm))

      doc should containText(messages("users.move_cases.team_or_user.header", 5))

    }
  }
}
