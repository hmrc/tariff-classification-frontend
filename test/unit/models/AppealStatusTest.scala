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

import models.AppealStatus.{ALLOWED, DISMISSED, IN_PROGRESS}
import models.AppealType.{ADR, APPEAL_TIER_1, REVIEW}
import uk.gov.hmrc.play.test.UnitSpec

class AppealStatusTest extends UnitSpec {

  "Appeal format" should {

    "format 'In Progress'" in {
      AppealStatus.formatAppeal(IN_PROGRESS) shouldBe "Under appeal"
    }

    "format 'allowed'" in {
      AppealStatus.formatAppeal(ALLOWED) shouldBe "Appeal allowed"
    }

    "format 'dismissed'" in {
      AppealStatus.formatAppeal(DISMISSED) shouldBe "Appeal dismissed"
    }
  }

  "Review format" should {

    "format 'In Progress'" in {
      AppealStatus.formatReview(IN_PROGRESS) shouldBe "Under review"
    }

    "format 'allowed'" in {
      AppealStatus.formatReview(ALLOWED) shouldBe "Review upheld"
    }

    "format 'dismissed'" in {
      AppealStatus.formatReview(DISMISSED) shouldBe "Review overturned"
    }
  }

  "Dispute format" should {

    "format 'In Progress'" in {
      AppealStatus.formatDispute(IN_PROGRESS) shouldBe "Under mediation"
    }

    "format 'allowed'" in {
      AppealStatus.formatDispute(ALLOWED) shouldBe "Completed"
    }

    "format 'dismissed'" in {
      AppealStatus.formatDispute(DISMISSED) shouldBe "Completed"
    }
  }

  "Format" should {

    "format 'ADR''" in {
      AppealStatus.format(ADR, IN_PROGRESS) shouldBe "Under mediation"
      AppealStatus.format(ADR, ALLOWED) shouldBe "Completed"
      AppealStatus.format(ADR, DISMISSED ) shouldBe "Completed"
    }

    "format 'Review''" in {
      AppealStatus.format(REVIEW, IN_PROGRESS) shouldBe "Under review"
      AppealStatus.format(REVIEW, ALLOWED) shouldBe "Review upheld"
      AppealStatus.format(REVIEW, DISMISSED ) shouldBe "Review overturned"
    }

    "format 'Appeal''" in {
      AppealStatus.format(APPEAL_TIER_1, IN_PROGRESS) shouldBe "Under appeal"
      AppealStatus.format(APPEAL_TIER_1, ALLOWED) shouldBe "Appeal allowed"
      AppealStatus.format(APPEAL_TIER_1, DISMISSED ) shouldBe "Appeal dismissed"
    }
  }

  "Appeal" should {

    "should allow all statuses" in {
      AppealStatus.validFor(APPEAL_TIER_1) shouldBe Seq(IN_PROGRESS, ALLOWED, DISMISSED)
    }
  }

  "Review" should {

    "should allow all statuses" in {
      AppealStatus.validFor(REVIEW) shouldBe Seq(IN_PROGRESS, ALLOWED, DISMISSED)
    }
  }

  "ADR" should {

    "should allow restricted statuses" in {
      AppealStatus.validFor(ADR) shouldBe Seq(IN_PROGRESS, ALLOWED)
    }
  }

}
