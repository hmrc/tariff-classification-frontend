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

import uk.gov.hmrc.tariffclassificationfrontend.models.{Appeal, AppealStatus}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.statuses.appeal_status
import uk.gov.tariffclassificationfrontend.utils.Cases
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withDecision, withReference}

class AppealStatusViewSpec extends ViewSpec {

  "Appeal Status" should {

    "render the appeal status if it is defined" in {
      // When
      val c = aCase(
        withReference("ref"),
        withDecision(appeal = Some(Appeal(AppealStatus.DISMISSED)))
      )

      val doc = view(appeal_status(c, "id"))

      // Then
      doc.text() shouldBe "Appeal dismissed"
    }

    "not render the appeal status if it is not defined" in {
      // When
      val doc = view(appeal_status(Cases.btiCaseExample, "id"))

      // Then
      doc.text() shouldBe ""
    }

  }

}
