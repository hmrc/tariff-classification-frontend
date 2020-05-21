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

package views

import utils.Cases
import views.html.{confirm_cancel_ruling, confirm_complete_case}

class ConfirmCompleteCaseViewSpec extends ViewSpec {

  "Confirm Complete page" should {

    "Render text for BTI" in {
      // When
      val c = Cases.btiCaseWithExpiredRuling
      val doc = view(confirm_complete_case(c))
      lazy val expected = "This case has been completed The ruling has been published. View the published ruling on the public rulings website. The applicant has been notified by email. The status of this case is now completed"
      lazy val actual = doc.getElementById("confirm_complete_id").text()

      // Then
      actual should startWith(expected)
    }

    "Render text for Liability" in {
      // When
      val c = Cases.aLiabilityCase()
      val doc = view(confirm_complete_case(c))
      lazy val expected = "This liability decision is complete Email the applicant with a copy of this decision."
      lazy val actual = doc.getElementById("confirm_complete_id").text()

      // Then
      actual should startWith(expected)
    }

  }


}
