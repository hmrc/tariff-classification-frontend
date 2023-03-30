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

import models.ApplicationType
import models.forms.v2.MoveCasesForm
import models.viewmodels.{ApplicationTabViewModel, ApplicationsTab}
import play.twirl.api.Html
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.partials.users.atar_table

class AtarTableViewSpec extends ViewSpec {
  private val moveATaRCasesForm = MoveCasesForm.moveCasesForm("atarCases")

  def atarTable(atarTab: ApplicationsTab): Html = atar_table(atarTab, moveATaRCasesForm, "1")

  val assignedToMeCasesTab: ApplicationTabViewModel =
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
  "Atar table View" should {
    "render successfully" in {
      val doc = view(
        atarTable(assignedToMeCasesTab.applicationTabs.find(a => a.applicationType.equals(ApplicationType.ATAR)).get)
      )

      doc should containElementWithID("applicationTab.atar-table")
      doc should containElementWithID("applicationTab.atar-select")
      doc should containElementWithID("applicationTab.atar-reference")
      doc should containElementWithID("applicationTab.atar-goods")
      doc should containElementWithID("applicationTab.atar-trader")
      doc should containElementWithID("applicationTab.atar-elapsed-days")
      doc should containElementWithID("applicationTab.atar-total-days")
      doc should containElementWithID("applicationTab.atar-status")
      doc should containElementWithID("move-atar-cases")

    }
  }

}
