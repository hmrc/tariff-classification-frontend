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
import models.viewmodels.{ApplicationTabViewModel, ApplicationsTab}
import play.twirl.api.Html
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.partials.users.correspondence_table

class CorrespondenceTableViewSpec extends ViewSpec {

  def corrTable(corrTab: ApplicationsTab): Html = correspondence_table(corrTab)

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
        corrTable(
          assignedToMeCasesTab.applicationTabs.find(a => a.applicationType.equals(ApplicationType.CORRESPONDENCE)).get
        )
      )

      doc should containElementWithID("applicationTab.correspondence-table")
      doc should containElementWithID("applicationTab.correspondence-select")
      doc should containElementWithID("applicationTab.correspondence-reference")
      doc should containElementWithID("applicationTab.correspondence-subject")
      doc should containElementWithID("applicationTab.correspondence-trader")
      doc should containElementWithID("applicationTab.correspondence-elapsed-days")
      doc should containElementWithID("applicationTab.correspondence-contact")
      doc should containElementWithID("move-cases")

    }
  }

}
