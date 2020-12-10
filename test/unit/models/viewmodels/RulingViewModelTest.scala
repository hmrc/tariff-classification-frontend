/*
 * Copyright 2020 HM Revenue & Customs
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

package models.viewmodels

import models.CaseStatus._
import models.{Case, CaseStatus, ModelsBaseSpec, Permission}
import utils.Cases

class RulingViewModelTest extends ModelsBaseSpec {

  val dummyCase: Case = Cases.liabilityCaseExample.copy(status = CaseStatus.NEW)

  "showEditRuling" should {

    "show edit ruling button when we have EDIT_RULING permission" in {
      val aCase                        = dummyCase.copy(status = OPEN)
      val permissions: Set[Permission] = Set(Permission.EDIT_RULING)
      RulingViewModel.fromCase(aCase, permissions).showEditRuling shouldBe true
    }

    "not show edit ruling button because permission is missing" in {
      val aCase                        = dummyCase.copy(status = OPEN)
      val permissions: Set[Permission] = Set()
      RulingViewModel.fromCase(aCase, permissions).showEditRuling shouldBe false
    }
  }

  "fromCase" should {

    "return a RulingViewModel when a case with decision is passed" in {
      val liabilityCase =
        Cases.liabilityCaseWithDecisionExample.copy(status = CaseStatus.OPEN, reference = "case reference")
      val rulingViewModel = RulingViewModel.fromCase(liabilityCase)

      val expected = RulingViewModel(
        commodityCodeEnteredByTraderOrAgent = "trader-1234567",
        commodityCodeSuggestedByOfficer     = "officer-1234567",
        commodityCode                       = "040900",
        itemDescription                     = "good description",
        justification                       = "justification",
        methodSearch                        = "",
        methodExclusion                     = "Excludes everything ever",
        showEditRuling                      = false,
        caseReference                       = "case reference"
      )

      rulingViewModel shouldBe expected
    }
  }
}
