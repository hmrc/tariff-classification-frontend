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

import config.AppConfig
import models.forms._
import models.{CaseStatus, CommodityCode}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatestplus.mockito.MockitoSugar
import service.CommodityCodeService
import utils.Cases
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.ruling.complete_ruling_section

class CompleteRulingSectionViewSpec extends ViewSpec with MockitoSugar {

  private val commodityCodeService = mock[CommodityCodeService]
  private val decisionForm = new DecisionForm(new CommodityCodeConstraints(commodityCodeService, mock[AppConfig]))

  "Complete ruling section" should {

    "render with disabled button for case with incomplete decision" in {
      given(commodityCodeService.find(anyString())).willReturn(None)
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

      // When
      val doc =
        view(
          complete_ruling_section(
            case1,
            Some(decisionForm.btiCompleteForm.bindFromRequest(
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
            ),
            commodityCode = Some(CommodityCode("lorum ipsum"))
          )
        )

      // Then
      doc should containElementWithAttribute("disabled", "disabled")
      doc should containElementWithID("complete-case-button")
    }

    "render with enabled button for case with complete decision" in {
      val case1 = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
      val mandatoryFieldForm = Some(decisionForm.btiCompleteForm)

      // When
      val doc = view(complete_ruling_section(c = case1, decisionForm = mandatoryFieldForm, commodityCode = Some(CommodityCode("lorum ipsum"))))

      // Then
      doc shouldNot containElementWithID("complete-case-button-disabled")
      doc should containElementWithID("complete-case-button")
      doc.getElementById("complete-case-button") should haveAttribute("href", "/manage-tariff-classifications/cases/1/complete")
    }

    "not render for cases with no decision" in {
      val c = Cases.btiCaseExample.copy(decision = None)

      // When
      val doc = view(complete_ruling_section(c = c, decisionForm = None, commodityCode = Some(CommodityCode("lorum ipsum"))))

      // Then
      doc shouldNot containElementWithID("complete-case-button-disabled")
      doc shouldNot containElementWithID("complete-case-button")
    }
  }

}
