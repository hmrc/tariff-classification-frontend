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

import controllers.ActiveTab.Liability
import models.CaseStatus
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases


class LiabilityViewModelSpec extends UnitSpec {

  "fromCase" should {

    "create a cancelled view model" in {

      val c = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

      assert(LiabilityViewModel.fromCase(c) === LiabilityViewModel(CaseHeaderViewModel("Liability", "trader-business-name", "Laptop", "1", "CANCELLED", false)))

    }


    "create a complete view model if it has an expired ruling" in {

      val c = Cases.btiCaseWithExpiredRuling

      assert(LiabilityViewModel.fromCase(c).caseHeaderViewModel.caseStatus === "EXPIRED")

    }

    "create a completed view model" in {

      val c = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)

      assert(LiabilityViewModel.fromCase(c).caseHeaderViewModel.caseStatus === "COMPLETED")

    }
  }

}
