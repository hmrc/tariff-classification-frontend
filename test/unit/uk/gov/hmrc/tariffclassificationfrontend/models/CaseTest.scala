/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.models

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.tariffclassificationfrontend.utils.Cases
import java.time.Instant

import org.scalatest.Assertion

class CaseTest extends UnitSpec {

  "Case 'rulingHasNotExpired'" should {

    "return false for cases without a decision" in {
      assertRulingHasExpired(None, expectedResult = false)
    }

    "return false when 'effectiveEndDate' is not defined" in {
      val dec = Cases.decision.copy(effectiveEndDate = None)

      assertRulingHasExpired(Some(dec), expectedResult = false)
    }

    "return false when 'effectiveEndDate' is before the current time" in {
      val endDate = Instant.now().plusSeconds(-10)
      val dec = Cases.decision.copy(effectiveEndDate = Some(endDate))

      assertRulingHasExpired(Some(dec), expectedResult = false)
    }

    "return true when 'effectiveEndDate' is after the current time" in {
      val endDate = Instant.now().plusSeconds(10)
      val dec = Cases.decision.copy(effectiveEndDate = Some(endDate))

      assertRulingHasExpired(Some(dec), expectedResult = true)
    }
  }

  private def assertRulingHasExpired(dec: Option[Decision], expectedResult: Boolean): Assertion = {
    Cases.btiCaseExample.copy(decision = dec).rulingHasNotExpired shouldBe expectedResult
  }

}
