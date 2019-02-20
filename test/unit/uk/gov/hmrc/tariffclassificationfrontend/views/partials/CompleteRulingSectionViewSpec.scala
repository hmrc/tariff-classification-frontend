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

import uk.gov.hmrc.tariffclassificationfrontend.forms._
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.complete_ruling_section
import uk.gov.tariffclassificationfrontend.utils.Cases

class CompleteRulingSectionViewSpec extends ViewSpec {

  "Complete ruling section" should {

    "render with disabled button for OPEN case with incomplete decision" in {
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

      // When
      val doc =
        view(
          complete_ruling_section(
            case1,
            Some(DecisionForm.mandatoryFieldsForm.bindFromRequest(
              Map(
                "goodsDescription" -> Seq.empty,
                "bindingCommodityCode" -> Seq("lorum ipsum"),
                "methodSearch" -> Seq("lorum ipsum"),
                "justification" -> Seq("lorum ipsum"),
                "methodCommercialDenomination" -> Seq.empty,
                "methodExclusion" -> Seq.empty,
                "attachments" -> Seq.empty
                )
              )
            )
          )
        )

      // Then
      doc should containElementWithID("complete-case-button-disabled")
      doc shouldNot containElementWithID("complete-case-button")
    }

    "render with enabled button for OPEN case with complete decision" in {
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
      val decisionForm = Some(DecisionForm.mandatoryFieldsForm)

      // When
      val doc = view(complete_ruling_section(case1, decisionForm))

      // Then
      doc shouldNot containElementWithID("complete-case-button-disabled")
      doc should containElementWithID("complete-case-button")
      doc.getElementById("complete-case-button") should haveAttribute("href", "/tariff-classification/cases/1/complete")
    }

    "not render for non-OPEN case statuses" in {
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

      // When
      val doc = view(complete_ruling_section(case1, None))

      // Then
      doc shouldNot containElementWithID("complete-case-button-disabled")
      doc shouldNot containElementWithID("complete-case-button")
    }


    "not render for OPEN cases with no decision" in {
      val c = Cases.btiCaseExample.copy(decision = None)

      // When
      val doc = view(complete_ruling_section(c, None))

      // Then
      doc shouldNot containElementWithID("complete-case-button-disabled")
      doc shouldNot containElementWithID("complete-case-button")
    }
  }

}
