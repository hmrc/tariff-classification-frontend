/*
 * Copyright 2025 HM Revenue & Customs
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

package views.partials.liabilities

import models._
import models.viewmodels.CaseViewModel
import utils.Cases
import utils.Cases._
import views.ViewMatchers.containElementWithID
import views.ViewSpec

class AdvancedSearchCasesViewSpec extends ViewSpec {

  private val showAdvancedSearchButtonStatuses    = Seq(CaseStatus.OPEN, CaseStatus.REFERRED, CaseStatus.SUSPENDED)
  private val notShowAdvancedSearchButtonStatuses = (CaseStatus.values -- showAdvancedSearchButtonStatuses).toList
  private val advanced_search_cases = app.injector.instanceOf[views.html.partials.liabilities.advanced_search_cases]

  "Advanced search button" should {

    showAdvancedSearchButtonStatuses.foreach { status =>
      s"render advanced search button when case status is '${status.toString}'" in {
        val c = aLiabilityCase(withReference("reference"), withStatus(status), withLiabilityApplication())

        val doc = view(
          advanced_search_cases(
            CaseViewModel.fromCase(c, Cases.operatorWithReleaseOrSuppressPermissions)
          )
        )

        doc                                                 should containElementWithID("advanced-search-button")
        doc.getElementById("advanced-search-button").text shouldBe messages("case.v2.liability.advanced_search.button")
      }
    }

    notShowAdvancedSearchButtonStatuses.foreach { status =>
      s"not render advanced search button when case status is '${status.toString}'" in {
        val c = aLiabilityCase(withReference("reference"), withStatus(status), withLiabilityApplication())

        val doc = view(
          advanced_search_cases(
            CaseViewModel.fromCase(c, Cases.operatorWithReleaseOrSuppressPermissions)
          )
        )

        doc shouldNot containElementWithID("advanced-search-button")
      }
    }

  }
}
