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

package views.partials

import models.response.ScanStatus
import models.{Contact, Permission}
import service.CountriesService
import utils.Cases
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.case_trader

class CaseTraderViewSpec extends ViewSpec {

  val requestWithReleaseAndSuppressPermission =
    requestWithPermissions(Permission.RELEASE_CASE, Permission.SUPPRESS_CASE)
  val countriesService = new CountriesService

  "Case Trader" should {

    "Not render agent details when not present" in {
      // Given
      val `case` = aCase(
        withReference("ref"),
        withoutAgent()
      )

      // When
      val doc = view(case_trader(`case`, 0, s => Some("dummy country name")))

      // Then
      doc shouldNot containElementWithID("agent-submitted-heading")
    }

    "render boards file number when present" in {
      // Given
      val c = aCase().copy(caseBoardsFileNumber = Some("file 123"))

      // When
      val doc = view(case_trader(c, 0, s => Some("dummy country name")))

      // Then
      val boardFileNumber = doc.getElementById("boards-file-number")
      boardFileNumber.text() shouldBe "file 123"
    }

    "not show boards file number when not present" in {
      // Given
      val c = aCase()

      // When
      val doc = view(case_trader(c, 0, s => Some("dummy country name")))

      // Then
      doc shouldNot containElementWithID("boards-file-number-label")
      doc shouldNot containElementWithID("boards-file-number")
    }

  }

}
