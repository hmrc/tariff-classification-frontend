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
import views.html.partials.users.done_move_cases

class DoneMoveCasesViewSpec extends ViewSpec {

  def done: done_move_cases = injector.instanceOf[done_move_cases]

  "Done move cases" should {
    "render successfully with only team present" in {
      val doc = view(done("name", "team", None, ManagerToolsUsersTab))

      doc should containElementWithID("manager-tools-cases-sub-nav")
      doc should containElementWithClass("govuk-panel govuk-panel--confirmation")
    }

    "render successfully with user and team present" in {
      val doc = view(done("name", "team", Some("new user"), ManagerToolsUsersTab))

      doc should containElementWithID("manager-tools-cases-sub-nav")
      doc should containElementWithClass("govuk-panel govuk-panel--confirmation")
    }
  }
}
