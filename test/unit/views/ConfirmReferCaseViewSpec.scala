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
import views.html.confirm_refer_case

class ConfirmReferCaseViewSpec extends ViewSpec {

  "Confirm Refer Case page" should {

    "Render text for BTI" in {
      // When
      val c   = Cases.btiCaseWithExpiredRuling
      val doc = view(confirm_refer_case(c))
      lazy val expected =
        "Case referred The days elapsed count has been paused at 0"
      lazy val actual = doc.getElementById("confirm_complete_id").text()

      // Then
      actual should startWith(expected)
    }

    "Render text for Liability" in {
      // When
      val c             = Cases.aLiabilityCase()
      val doc           = view(confirm_refer_case(c))
      lazy val expected = "Case referred The days elapsed count has been paused at 0"
      lazy val actual   = doc.getElementById("confirm_complete_id").text()

      // Then
      actual should startWith(expected)
    }

    "Render text for Correspondence" in {
      // When
      val c             = Cases.aCorrespondenceCase()
      val doc           = view(confirm_refer_case(c))
      lazy val expected = "Case referred The days elapsed count has been paused at 0"
      lazy val actual   = doc.getElementById("confirm_complete_id").text()

      // Then
      actual should startWith(expected)
    }

    "Render text for Miscellaneous" in {
      // When
      val c             = Cases.aMiscellaneousCase()
      val doc           = view(confirm_refer_case(c))
      lazy val expected = "Case referred The days elapsed count has been paused at 0"
      lazy val actual   = doc.getElementById("confirm_complete_id").text()

      // Then
      actual should startWith(expected)
    }

  }

}
