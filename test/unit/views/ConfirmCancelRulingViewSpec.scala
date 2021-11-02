/*
 * Copyright 2021 HM Revenue & Customs
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

class ConfirmCancelRulingViewSpec extends ViewSpec {

  val confirmCancelRulingView = app.injector.instanceOf[confirm_cancel_ruling]

  "Confirm Cancel page" should {

    "Render text for BTI" in {
      // When
      val c   = Cases.btiCaseWithExpiredRuling
      val doc = view(confirmCancelRulingView(c))
      lazy val expected =
        "This ruling has been removed from the Search for Advance Tariff Rulings website"
      lazy val actual = doc.getElementById("main-content").text()

      // Then
      actual should include(expected)
    }

    "Render text for Liability" in {
      // When
      val c             = Cases.aLiabilityCase()
      val doc           = view(confirmCancelRulingView(c))
      lazy val expected = "The liability decision has been cancelled"
      lazy val actual   = doc.getElementById("main-content").text()

      // Then
      actual should include(expected)
    }

  }

}
