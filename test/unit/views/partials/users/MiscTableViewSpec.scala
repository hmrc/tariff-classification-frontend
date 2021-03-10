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

import models.ApplicationType
import models.forms.v2.MoveCasesForm
import models.viewmodels.{ApplicationTabViewModel, ApplicationsTab}
import play.twirl.api.Html
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.partials.users.misc_table

class MiscTableViewSpec extends ViewSpec {
  private val moveMiscCasesForm = MoveCasesForm.moveCasesForm("miscCases")

  def miscTable(miscTab: ApplicationsTab): Html = misc_table(miscTab, moveMiscCasesForm, "1")

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
  "Misc table View" should {
    "render successfully" in {
      val doc = view(
        miscTable(
          assignedToMeCasesTab.applicationTabs.find(a => a.applicationType.equals(ApplicationType.MISCELLANEOUS)).get
        )
      )

      doc should containElementWithID("applicationTab.miscellaneous-table")
      doc should containElementWithID("applicationTab.miscellaneous-select")
      doc should containElementWithID("applicationTab.miscellaneous-reference")
      doc should containElementWithID("applicationTab.miscellaneous-subject")
      doc should containElementWithID("applicationTab.miscellaneous-trader")
      doc should containElementWithID("applicationTab.miscellaneous-elapsed-days")
      doc should containElementWithID("applicationTab.miscellaneous-type")
      doc should containElementWithID("move-misc-cases")

    }
  }

}
