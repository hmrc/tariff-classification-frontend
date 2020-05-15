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

package models

import java.util.UUID

import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases._

class AppealTest extends UnitSpec {

  private def randomId(): String = UUID.randomUUID().toString

  "highestAppealFromDecision" should {

    "return None if None is passed" in {
      //  Given
      val c = aCase(
        withoutDecision()
      )

      // When
      val res = Appeal.highestAppealFromDecision(c.decision)

      // Then
      res shouldBe None
    }

    "return None if appeals list is passed as empty" in {
      //  Given
      val c = aCase(
        withDecision(appeal = Seq())
      )

      // When
      val res = Appeal.highestAppealFromDecision(c.decision)

      // Then
      res shouldBe None
    }

    "return ADR if appeals list is passed only with ADR" in {
      //  Given
      val expected = Some(Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.ADR))
      val c = aCase(
        withDecision(appeal = Seq(
          expected.get
        ))
      )

      // When
      val res = Appeal.highestAppealFromDecision(c.decision)

      // Then
      res shouldBe expected
    }

    "return REVIEW if appeals list is passed with ADR and REVIEW" in {
      //  Given
      val expected = Some(Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.REVIEW))

      val c = aCase(
        withDecision(appeal = Seq(
          Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.ADR),
          expected.get
        ))
      )

      // When
      val res = Appeal.highestAppealFromDecision(c.decision)

      // Then
      res shouldBe expected
    }

    "return SUPREME_COURT if appeals list is passed with all elements from enum" in {
      //  Given
      val expected = Some(Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.SUPREME_COURT))

      val c = aCase(
        withDecision(appeal = Seq(
          Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.ADR),
          Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.REVIEW),
          Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1),
          Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_2),
          Appeal(randomId(), AppealStatus.IN_PROGRESS, AppealType.COURT_OF_APPEALS),
          expected.get
        ))
      )

      // When
      val res = Appeal.highestAppealFromDecision(c.decision)

      // Then
      res shouldBe expected
    }

  }

}
