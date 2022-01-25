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

package views

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

import utils.{Cases, Dates}
import views.html.confirm_complete_case

class ConfirmCompleteCaseViewSpec extends ViewSpec {

  val confirmCompleteCaseView = app.injector.instanceOf[confirm_complete_case]

  "Confirm Complete page" should {

    "Render text for BTI" in {
      // When
      val c   = Cases.btiCaseWithExpiredRuling
      val doc = view(confirmCompleteCaseView(c))
      lazy val expected =
        s"The Laptop ATaR case has been completed A ruling certificate has been created with an expiry date of ${Dates
          .format(Instant.now().plus(-10, DAYS))} Next steps Back to home Back to open ATaR cases"
      lazy val actual = doc.getElementById("main-content").text()

      // Then
      actual should startWith(expected)
    }

    "Render text for Liability" in {
      // When
      val c   = Cases.aLiabilityCase()
      val doc = view(confirmCompleteCaseView(c))
      lazy val expected =
        "The good-name Liability case has been completed Next steps Back to home Back to open Liability cases"
      lazy val actual = doc.getElementById("main-content").text()

      // Then
      actual should startWith(expected)
    }

    "Render text for Correspondence" in {
      // When
      val c   = Cases.aCorrespondenceCase()
      val doc = view(confirmCompleteCaseView(c))
      lazy val expected =
        "The A short summary Correspondence case has been completed Next steps Back to home Back to open Correspondence cases"
      lazy val actual = doc.getElementById("main-content").text()

      // Then
      actual should startWith(expected)
    }

    "Render text for Miscellaneous" in {
      // When
      val c   = Cases.aMiscellaneousCase()
      val doc = view(confirmCompleteCaseView(c))
      lazy val expected =
        "The name Miscellaneous case has been completed Next steps Back to home Back to open Miscellaneous cases"
      lazy val actual = doc.getElementById("main-content").text()

      // Then
      actual should startWith(expected)
    }

  }

}
