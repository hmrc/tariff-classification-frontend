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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.case_heading
import uk.gov.tariffclassificationfrontend.utils.Cases._

class CaseHeadingViewSpec extends ViewSpec {

  "Case Heading" should {

    "Render" in {
      // Given
      val c = aCase(
        withReference("ref"),
        withStatus(CaseStatus.OPEN)
      )

      // When
      val doc = view(case_heading(c))

      // Then
      doc should containElementWithID("case-reference")
      doc.getElementById("case-reference") should containText("Case ref")
      doc should containElementWithID("case-status")
      doc.getElementById("case-status") should containText("OPEN")
    }

  }

}
