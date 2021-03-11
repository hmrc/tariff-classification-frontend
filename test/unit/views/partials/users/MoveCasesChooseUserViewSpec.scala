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

import models.Operator
import models.forms.v2.UserToMoveCaseForm
import models.viewmodels.ManagerToolsUsersTab
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.users.move_cases_choose_user

class MoveCasesChooseUserViewSpec extends ViewSpec {

  def chooseUser: move_cases_choose_user = injector.instanceOf[move_cases_choose_user]
  private val chooseUserForm             = UserToMoveCaseForm.form
  private val operators                  = Seq(Operator("1"))

  "Choose user" should {
    "render successfully with one case" in {
      val doc = view(chooseUser(1, operators, chooseUserForm, None, ManagerToolsUsersTab))

      doc should containElementWithID("manager-tools-cases-sub-nav")
      doc should containText(messages("users.move_cases.choose_user.header.single"))
    }

    "render successfully with multiple cases" in {
      val doc = view(chooseUser(5, operators, chooseUserForm, None, ManagerToolsUsersTab))

      doc should containElementWithID("manager-tools-cases-sub-nav")
      doc should containText(messages("users.move_cases.choose_user.header", 5))

    }
  }
}
