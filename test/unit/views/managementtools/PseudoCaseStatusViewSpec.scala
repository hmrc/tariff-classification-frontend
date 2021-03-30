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

package views.managementtools

import models._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.pseudoCaseStatus

class PseudoCaseStatusViewSpec extends ViewSpec {

  "pseudoCaseStatus view" should {
    "render with the specified ID" in {
      val doc = view(pseudoCaseStatus(PseudoCaseStatus.COMPLETED, "test-pseudo-status"))
      doc should containElementWithID("test-pseudo-status")
    }

    "render with the appropriate label" in {
      for (status <- PseudoCaseStatus.values) {
        val doc = view(pseudoCaseStatus(status, "test-pseudo-status"))
        doc.getElementById("test-pseudo-status") should containText(PseudoCaseStatus.format(status))
      }
    }
  }
}
