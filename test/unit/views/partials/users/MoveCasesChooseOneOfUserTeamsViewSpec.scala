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

import models.forms.v2.TeamToMoveCaseForm
import models.viewmodels.ManagerToolsUsersTab
import models.{Operator, Queues}
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.users.move_cases_choose_one_from_user_teams

class MoveCasesChooseOneOfUserTeamsViewSpec extends ViewSpec {

  def chooseTeam: move_cases_choose_one_from_user_teams = injector.instanceOf[move_cases_choose_one_from_user_teams]
  private val chooseTeamForm                            = TeamToMoveCaseForm.form
  private val operators                                 = Seq(Operator("1"))

  "Choose one of user teams" should {
    "render successfully" in {
      val doc = view(chooseTeam("name", 1, 3, chooseTeamForm, Queues.allDynamicQueues))

      doc should containElementWithID("move-to-user-team")
    }
  }
}
