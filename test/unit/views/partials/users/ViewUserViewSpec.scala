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

package views.partials.users

import models.forms.v2.MoveCasesForm
import models.viewmodels.{ApplicationTabViewModel, ApplicationsTab}
import models.{Operator, Queues, Role}
import utils.Cases
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.users.view_user

class ViewUserViewSpec extends ViewSpec {
  private val moveATaRCasesForm = MoveCasesForm.moveCasesForm("atarCases")
  private val moveLiabCasesForm = MoveCasesForm.moveCasesForm("liabilityCases")
  private val moveCorrCasesForm = MoveCasesForm.moveCasesForm("corrCases")
  private val moveMiscCasesForm = MoveCasesForm.moveCasesForm("miscCases")
  def viewUser: view_user       = injector.instanceOf[view_user]
  val userWithNoNameAndNoTeam   = Operator("1")
  val assignedToMeCasesTab =
    ApplicationTabViewModel(
      "message key",
      ApplicationsTab
        .casesByTypes(
          Seq(
            Cases.btiCaseExample,
            Cases.liabilityCaseExample,
            Cases.correspondenceCaseExample,
            Cases.miscellaneousCaseExample
          )
        )
        .applicationTabs
    )

  "viewUser View" should {
    "render successfully" in {
      val doc = view(
        viewUser(
          userWithNoNameAndNoTeam,
          assignedToMeCasesTab,
          moveATaRCasesForm,
          moveLiabCasesForm,
          moveCorrCasesForm,
          moveMiscCasesForm
        )
      )

      doc should containElementWithID("user_details_tab")
      doc should containElementWithID("correspondence_tab")
      doc should containElementWithID("miscellaneous_tab")
      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
    }
  }
}
