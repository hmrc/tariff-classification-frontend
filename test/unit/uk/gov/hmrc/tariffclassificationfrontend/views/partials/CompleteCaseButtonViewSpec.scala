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
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.complete_case_button
import uk.gov.tariffclassificationfrontend.utils.Cases

class CompleteCaseButtonViewSpec extends ViewSpec {

  "Complete Case Button" should {

    "render button for OPEN case" in {
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

      // When
      val doc = view(complete_case_button(case1))

      // Then
      doc should containElementWithID("complete-case-button")
      doc.getElementById("complete-case-button") should haveAttribute("href", "/tariff-classification/cases/1/complete")
    }

    "not render button for other case status" in {
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

      // When
      val doc = view(complete_case_button(case1))

      // Then
      doc shouldNot containElementWithID("complete-case-button")
    }
  }

}
