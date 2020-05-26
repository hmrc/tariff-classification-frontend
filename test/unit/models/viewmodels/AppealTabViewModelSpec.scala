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

import models._
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases

class AppealTabViewModelSpec extends UnitSpec {

  val dummyCase: Case = Cases.aCaseWithCompleteDecision.copy(
    status = CaseStatus.COMPLETED,
    decision = Some(Cases.decision.copy(
      appeal = Seq(Appeal("id",
        AppealStatus.IN_PROGRESS,
        AppealType.APPEAL_TIER_1)),
      cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true)))
    )
  )

  "AppealTabViewModel fromCase" should {

    "return an AppealTabViewModel when a case has been passed" in {

      val appealTabViewModel = AppealTabViewModel.fromCase(dummyCase, Cases.operatorWithPermissions)

      val expected = AppealTabViewModel(caseReference = "123456",
        appeals = Seq(Appeal("id",
          AppealStatus.IN_PROGRESS,
          AppealType.APPEAL_TIER_1)),
        None,
        false)

      appealTabViewModel shouldBe expected
    }
  }
}
