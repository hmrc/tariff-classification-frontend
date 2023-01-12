/*
 * Copyright 2023 HM Revenue & Customs
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

package models.forms

import models.{Case, Decision, ModelsBaseSpec}
import utils.Cases._
import utils.DecisionForms

class DecisionFormMapperSpec extends ModelsBaseSpec {

  private val validForm = DecisionForms.validForm
  private val testCase  = btiCaseExample

  private val mapper = new DecisionFormMapper

  "Merge decision form into case object" should {

    "return valid Case with and amended decision from the form entries " in {

      val result = mapper.mergeFormIntoCase(testCase, validForm)

      compareAllFields(validForm, result.decision.get)
    }

    "return valid Case with new decision object filled from the form entries" in {

      val result: Case = mapper.mergeFormIntoCase(
        testCase.copy(decision = Option.empty),
        validForm
      )

      compareAllFields(validForm, result.decision.get)
    }

    "make attachments publishToRulings when they are contained in the form" in {

      val attToPublish                = attachment("url.to.publish")
      val attNotPublish               = attachment("url.to.not.be.published")
      val caseWithAtt                 = testCase.copy(attachments = Seq(attToPublish, attNotPublish))
      val decisionFormWithAttSelected = validForm.copy(attachments = Seq(attToPublish.id))

      val result: Case = mapper.mergeFormIntoCase(caseWithAtt, decisionFormWithAttSelected)

      val attachments = result.attachments.filter(_.shouldPublishToRulings).toList

      attachments should contain only attToPublish
    }
  }

  "case to decision form data mapper " should {

    "create valid decision form from a valid case " in {

      val caseWithAtt              = testCase.copy(attachments = Seq(attachment("url.to.publish")))
      val result: DecisionFormData = mapper.caseToDecisionFormData(caseWithAtt)

      compareAllFields(result, testCase.decision.get)
      result.attachments shouldBe Seq("url.to.publish")
    }

    "create empty decision form when a case does not have a decision" in {

      val empty                    = ""
      val caseWithEmptyDecision    = testCase.copy(decision = Option.empty)
      val result: DecisionFormData = mapper.caseToDecisionFormData(caseWithEmptyDecision)

      result.bindingCommodityCode         shouldBe empty
      result.goodsDescription             shouldBe empty
      result.justification                shouldBe empty
      result.methodSearch                 shouldBe empty
      result.methodExclusion              shouldBe empty
      result.methodCommercialDenomination shouldBe empty
      result.attachments                  shouldBe Seq.empty
      result.explanation                  shouldBe empty
    }

  }

  private def compareAllFields(form: DecisionFormData, decision: Decision): Unit = {
    decision.bindingCommodityCode                       shouldBe form.bindingCommodityCode
    decision.goodsDescription                           shouldBe form.goodsDescription
    decision.justification                              shouldBe form.justification
    decision.methodSearch.getOrElse("")                 shouldBe form.methodSearch
    decision.methodExclusion.getOrElse("")              shouldBe form.methodExclusion
    decision.methodCommercialDenomination.getOrElse("") shouldBe form.methodCommercialDenomination
    decision.explanation.getOrElse("")                  shouldBe form.explanation
  }

}
