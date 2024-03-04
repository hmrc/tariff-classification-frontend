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

package views

import utils.Cases
import views.html.confirm_refer_case

class ConfirmReferCaseViewSpec extends ViewSpec {

  val confirmReferCaseView: confirm_refer_case = app.injector.instanceOf[confirm_refer_case]

  "Confirm Refer Case page" should {

    "Render text for BTI" in {

      val c   = Cases.btiCaseWithExpiredRuling
      val doc = view(confirmReferCaseView(c))
      lazy val expected =
        "case has been referred The elapsed days count is paused at 0"
      lazy val actual = doc.getElementById("confirm_complete_id").text()

      actual should include(expected)
    }

    "Render text for Liability" in {

      val c             = Cases.aLiabilityCase()
      val doc           = view(confirmReferCaseView(c))
      lazy val expected = "case has been referred The elapsed days count is paused at 0"
      lazy val actual   = doc.getElementById("confirm_complete_id").text()

      actual should include(expected)
    }

    "Render text for Correspondence" in {

      val c             = Cases.aCorrespondenceCase()
      val doc           = view(confirmReferCaseView(c))
      lazy val expected = "case has been referred The elapsed days count is paused at 0"
      lazy val actual   = doc.getElementById("confirm_complete_id").text()

      actual should include(expected)
    }

    "Render text for Miscellaneous" in {

      val c             = Cases.aMiscellaneousCase()
      val doc           = view(confirmReferCaseView(c))
      lazy val expected = "case has been referred The elapsed days count is paused at 0"
      lazy val actual   = doc.getElementById("confirm_complete_id").text()

      actual should include(expected)
    }

  }

}
