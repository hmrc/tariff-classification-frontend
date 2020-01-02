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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.ruling

import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.ruling.ruling_explanation
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withDecision, withStatus, withoutDecision}

class RulingExplanationViewSpec extends ViewSpec {

  "Ruling explanation" should {

    "hide the `Holder information` section when the case has no decision" in {
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withoutDecision()
      )

      // When
      val doc = view(ruling_explanation(c))

      // Then
      doc shouldNot containElementWithID("information-for-holder")
    }

    "hide the `Holder explanation` field value when the decision has a blank explanation" in {
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withDecision(
          explanation = Some("")
        )
      )

      // When
      val doc = view(ruling_explanation(c))

      // Then
      doc shouldNot containElementWithID("holder_explanationValue")
    }

    "hide the `Holder explanation` field value when the decision has no explanation" in {
      val c = aCase(
        withStatus(CaseStatus.OPEN)
      )

      // When
      val doc = view(ruling_explanation(c))

      // Then
      doc shouldNot containElementWithID("holder_explanationValue")
    }

    "show the `Holder explanation` field value when the decision has an explanation" in {
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withDecision(
          explanation = Some("An explanation")
        )
      )

      // When
      val doc = view(ruling_explanation(c))

      // Then
      doc should containElementWithID("information-for-holder")
      doc should containElementWithID("holder_explanationValue")
      doc.getElementById("binding-commodity-code") should containText("decision-commodity-code")
    }


    "show a message when the commodity code is blank" in {
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withDecision(
          explanation = Some("An explanation"),
          bindingCommodityCode = ""
        )
      )

      // When
      val doc = view(ruling_explanation(c))

      // Then
      doc.getElementById("binding-commodity-code") should containText(messages("case.decision.explanation.commodityCode"))
    }

  }
}