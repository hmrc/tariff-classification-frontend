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

package views.partials.statuses

import models.{Appeal, AppealStatus, AppealType}
import utils.Cases.{aCase, withDecision, withReference}
import views.ViewSpec
import views.html.partials.statuses.appeal_status

class AppealStatusViewSpec extends ViewSpec {

  "Appeal Status" should {

    "render nothing for no appeals" in {
      //  Given
      val c = aCase(
        withReference("ref"),
        withDecision(appeal = Seq.empty)
      )

      val doc = view(appeal_status(c, "id"))

      doc.text() shouldBe ""
    }

    // ADR
    "render under mediation for IN_PROGRESS ADR" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal =
          Seq(
            Appeal("id", AppealStatus.IN_PROGRESS, AppealType.ADR)
          )
        )
      )

      val doc = view(appeal_status(c, "id"))

      doc.text() shouldBe "UNDER MEDIATION"
    }

    "render completed for ALLOWED ADR" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal =
          Seq(
            Appeal("id", AppealStatus.ALLOWED, AppealType.ADR)
          )
        )
      )

      val doc = view(appeal_status(c, "id"))

      doc.text() shouldBe "COMPLETED"
    }

    "render complected for DISMISSED ADR" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal =
          Seq(
            Appeal("id", AppealStatus.DISMISSED, AppealType.ADR)
          )
        )
      )

      val doc = view(appeal_status(c, "id"))

      doc.text() shouldBe "COMPLETED"
    }

    // REVIEW
    "render under review for IN_PROGRESS REVIEW" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal =
          Seq(
            Appeal("id", AppealStatus.IN_PROGRESS, AppealType.REVIEW)
          )
        )
      )

      val doc = view(appeal_status(c, "id"))

      doc.text() shouldBe "UNDER REVIEW"
    }

    "render review upheld for ALLOWED REVIEW" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal =
          Seq(
            Appeal("id", AppealStatus.ALLOWED, AppealType.REVIEW)
          )
        )
      )

      val doc = view(appeal_status(c, "id"))

      doc.text() shouldBe "REVIEW UPHELD"
    }

    "render review overturned for DISMISSED REVIEW" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal =
          Seq(
            Appeal("id", AppealStatus.DISMISSED, AppealType.REVIEW)
          )
        )
      )

      val doc = view(appeal_status(c, "id"))

      doc.text() shouldBe "REVIEW OVERTURNED"
    }

  }

}
