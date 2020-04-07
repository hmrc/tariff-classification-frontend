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

import models.CaseStatus
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases

class RulingViewModelTest extends UnitSpec {

  "fromCase" should {

    "return a RulingViewModel when a case with decision is passed" in {
      val liabilityCase = Cases.liabilityCaseWithDecisionExample
      val rulingViewModel = RulingViewModel.fromCase(liabilityCase)

      val expected = RulingViewModel(
        commodityCodeEnteredByTraderOrAgent = "trader-1234567",
        commodityCodeSuggestedByOfficer = "officer-1234567",
        commodityCode = "040900",
        itemDescription = "good description",
        justification = "justification",
        methodSearch = "",
        methodExclusion = "Excludes everything ever",
        status = CaseStatus.OPEN)

      rulingViewModel shouldBe expected
    }
  }
}
