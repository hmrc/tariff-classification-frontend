/*
 * Copyright 2022 HM Revenue & Customs
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

package views.partials

import models.CaseStatus
import utils.Cases
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.ruling.cancel_ruling_section
import models.viewmodels.atar.RulingTabViewModel

class CancelRulingButtonViewSpec extends ViewSpec {

  private lazy val cancelButtonId = "cancel-ruling-button"

  "Cancel Ruling Button" should {

    "render button for OPEN case" in {
      val c = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)
      val rulingTab = RulingTabViewModel.fromCase(c)

      // When
      val doc = view(cancel_ruling_section(rulingTab))

      // Then
      doc should containElementWithID(cancelButtonId)
      doc.getElementById(cancelButtonId) should haveAttribute(
        "href",
        controllers.routes.CancelRulingController.getCancelRulingReason(c.reference).path
      )
    }

    "not render button for other case status" in {
      val c = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
      val rulingTab = RulingTabViewModel.fromCase(c)

      // When
      val doc = view(cancel_ruling_section(rulingTab))

      // Then
      doc shouldNot containElementWithID(cancelButtonId)
    }

    "not render button for expired rulings" in {
      val c = Cases.btiCaseWithExpiredRuling
      val rulingTab = RulingTabViewModel.fromCase(c)

      // When
      val doc = view(cancel_ruling_section(rulingTab))

      // Then
      doc shouldNot containElementWithID(cancelButtonId)
    }
  }

}
