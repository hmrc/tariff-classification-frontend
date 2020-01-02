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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.ruling.cancel_ruling_section
import uk.gov.tariffclassificationfrontend.utils.Cases

class CancelRulingButtonViewSpec extends ViewSpec {

  private lazy val cancelButtonId = "cancel-ruling-button"

  "Cancel Ruling Button" should {

    "render button for OPEN case" in {
      val c = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)

      // When
      val doc = view(cancel_ruling_section(c))

      // Then
      doc should containElementWithID(cancelButtonId)
      doc.getElementById(cancelButtonId) should haveAttribute("href", "/tariff-classification/cases/1/ruling/cancel")
    }

    "not render button for other case status" in {
      val c = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

      // When
      val doc = view(cancel_ruling_section(c))

      // Then
      doc shouldNot containElementWithID(cancelButtonId)
    }

    "not render button for expired rulings" in {
      val c = Cases.btiCaseWithExpiredRuling

      // When
      val doc = view(cancel_ruling_section(c))

      // Then
      doc shouldNot containElementWithID(cancelButtonId)
    }
  }

}
