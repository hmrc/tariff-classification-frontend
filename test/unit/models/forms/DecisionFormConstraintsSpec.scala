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

package models.forms

import config.AppConfig
import models.{CommodityCode, ModelsBaseSpec}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, JsString, JsValue}
import service.CommodityCodeService

class DecisionFormConstraintsSpec extends ModelsBaseSpec {

  private val commodityCodeService = mock[CommodityCodeService]
  private val decisionForm         = new DecisionForm(new CommodityCodeConstraints(commodityCodeService, mock[AppConfig]))
  private val commodityCodeNumericErrorMessage = "decision_form.error.bindingCommodityCode.numeric"
  private val bindingCommodityCodeElementId     = "bindingCommodityCode"

  "DecisionForm validation" should {

    when(commodityCodeService.find(any())).thenReturn(Some(CommodityCode("code")))

    "pass if the commodity code is empty" in {
      assertNoErrors("")
    }

    "pass if the commodity code value contains between 6 and 22 digits and has an even number of digits" in {
      assertNoErrors("0409000000")
    }

    "pass if the commodity code value contains 6 digits" in {
      assertNoErrors("040900")
    }

    "pass if the commodity code value contains 22 digits" in {
      assertNoErrors("0409000000234567890000")
    }

    "fail if the commodity code value contains non numeric characters" in {
      assertOnlyOneError("12345Q", Seq(commodityCodeNumericErrorMessage))
    }

    "fail if the commodity code value contains special characters" in {
      assertOnlyOneError("12345!", Seq(commodityCodeNumericErrorMessage))
    }
  }

  private def commodityCodeJsValue(value: String): JsValue =
    JsObject(Seq(bindingCommodityCodeElementId -> JsString(value)))

  private def assertNoErrors(commodityCodeValue: String): Unit = {
    val errors =
      decisionForm.btiForm.bind(commodityCodeJsValue(commodityCodeValue)).errors(bindingCommodityCodeElementId)
    errors shouldBe Seq.empty
  }

  private def assertOnlyOneError(commodityCodeValue: String, errorMessages: Seq[String]): Unit = {
    val errors =
      decisionForm.btiForm.bind(commodityCodeJsValue(commodityCodeValue)).errors(bindingCommodityCodeElementId)
    errors.map(_.message) shouldBe errorMessages
  }

}
