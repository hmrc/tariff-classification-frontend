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

import java.time.Instant

import models.{Appeal, Case, CaseStatus}
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases
import utils.Cases.liabilityApplicationExample

class AppealTabViewModelSpec extends UnitSpec {

  val dummyCase: Case = Cases.aCaseWithCompleteDecision.copy(status = CaseStatus.COMPLETED)

  val liabilityCaseExample = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, None, decision = Some(), liabilityApplicationExample)

  "AppealTabViewModel fromCase" should {
    "return an AppealTabViewModel when a case has been passed" in {

      val appealTabViewModel = RulingViewModel.fromCase(dummyCase)

      val expected = AppealTabViewModel(caseReference = "123456",
        appeals = Seq.empty,
        showApplicationForExtendedUse = true,
        permissionForExtendedUse = true)

      appealTabViewModel shouldBe expected
    }
  }
}
