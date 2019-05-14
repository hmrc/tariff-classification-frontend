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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.statuses

import uk.gov.hmrc.tariffclassificationfrontend.models.{Appeal, AppealStatus, AppealType}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.statuses.appeal_status
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withDecision, withReference}

class AppealStatusViewSpec extends ViewSpec {

  "Appeal Status" should {

    "render nothing for no appeals" in {
      //  Given
      val c = aCase(
        withReference("ref"),
        withDecision(appeal = Seq.empty)
      )

      // When
      val doc = view(appeal_status(c, "id"))

      // Then
      doc.text() shouldBe ""
    }

    "render under appeal" in {
      // Given
      val c = aCase(
        withReference("ref"),
        withDecision(appeal = Seq(
          Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1),
          Appeal("id", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1),
          Appeal("id", AppealStatus.DISMISSED, AppealType.APPEAL_TIER_1)
        ))
      )

      // When
      val doc = view(appeal_status(c, "id"))

      // Then
      doc.text() shouldBe "Under appeal"
    }

    "render appeal allowed" in {
      // Given
      val c = aCase(
        withReference("ref"),
        withDecision(appeal = Seq(
          Appeal("id", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1),
          Appeal("id", AppealStatus.DISMISSED, AppealType.APPEAL_TIER_1)
        ))
      )

      // When
      val doc = view(appeal_status(c, "id"))

      // Then
      doc.text() shouldBe "Appeal allowed"
    }

    "render appeal dismissed" in {
      // Given
      val c = aCase(
        withReference("ref"),
        withDecision(appeal = Seq(
          Appeal("id", AppealStatus.DISMISSED, AppealType.APPEAL_TIER_1)
        ))
      )

      // When
      val doc = view(appeal_status(c, "id"))

      // Then
      doc.text() shouldBe "Appeal dismissed"
    }

  }

}
