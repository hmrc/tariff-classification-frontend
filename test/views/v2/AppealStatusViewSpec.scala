/*
 * Copyright 2024 HM Revenue & Customs
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

package views.v2

import models.{Appeal, AppealStatus, AppealType}
import utils.Cases.{aCase, withDecision, withReference}
import views.ViewSpec
import views.html.v2.appeal_status

class AppealStatusViewSpec extends ViewSpec {
  "Appeal Status" should {

    "render nothing for no appeals" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal = Seq.empty)
      )

      val doc = view(appeal_status(c.decision, "id"))
      doc.text() shouldBe ""
    }

    "render under mediation for IN_PROGRESS ADR" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal =
          Seq(
            Appeal("id", AppealStatus.IN_PROGRESS, AppealType.ADR)
          )
        )
      )

      val doc = view(appeal_status(c.decision, "id"))
      doc.text() shouldBe "Under mediation"
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

      val doc = view(appeal_status(c.decision, "id"))
      doc.text() shouldBe "Completed"
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

      val doc = view(appeal_status(c.decision, "id"))
      doc.text() shouldBe "Completed"
    }

    "render under review for IN_PROGRESS REVIEW" in {

      val c = aCase(
        withReference("ref"),
        withDecision(appeal =
          Seq(
            Appeal("id", AppealStatus.IN_PROGRESS, AppealType.REVIEW)
          )
        )
      )

      val doc = view(appeal_status(c.decision, "id"))
      doc.text() shouldBe "Under review"
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

      val doc = view(appeal_status(c.decision, "id"))
      doc.text() shouldBe "Review upheld"
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

      val doc = view(appeal_status(c.decision, "id"))
      doc.text() shouldBe "Review overturned"
    }
  }
}
