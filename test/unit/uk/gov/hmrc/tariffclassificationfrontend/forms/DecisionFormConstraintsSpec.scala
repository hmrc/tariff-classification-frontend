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

package uk.gov.hmrc.tariffclassificationfrontend.forms

import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import uk.gov.hmrc.play.test.UnitSpec

class DecisionFormConstraintsSpec extends UnitSpec {

  private val commodityCodeErrorMessage = "Format must be empty or numeric between 6 and 22 digits"
  private val decisionForm: Form[DecisionFormData] = DecisionForm.form
  private val bindingCommodityCodeElementId = "bindingCommodityCode"

  "DecisionForm validation" should {

    "pass if the commodity code is empty" in {
      assertNoErrors("")
    }

    "pass if the commodity code value contains between 6 and 22 digits" in {
      assertNoErrors("1234567890")
    }

    "pass if the commodity code value contains 6 digits" in {
      assertNoErrors("123456")
    }

    "pass if the commodity code value contains 22 digits" in {
      assertNoErrors("1234567891234567890000")
    }

    "fail if the commodity code value contains more than 22 digits" in {
      assertOnlyOneError("12345678901234567890123")
    }

    "fail if the commodity code value contains less than 6 digits" in {
      assertOnlyOneError("12345")
    }

    "fail if the commodity code value contains non numeric characters" in {
      assertOnlyOneError("12345Q")
    }

    "fail if the commodity code value contains special characters"  in {
      assertOnlyOneError("12345!")
    }

  }

  private def commodityCodeJsValue(value: String): JsValue = {
    JsObject(Seq(bindingCommodityCodeElementId -> JsString(value)))
  }

  private def assertNoErrors(commodityCodeValue: String): Unit = {
    val errors = decisionForm.bind(commodityCodeJsValue(commodityCodeValue)).errors(bindingCommodityCodeElementId)
    errors shouldBe Seq.empty
  }

  private def assertOnlyOneError(commodityCodeValue: String): Unit = {
    val errors = decisionForm.bind(commodityCodeJsValue(commodityCodeValue)).errors(bindingCommodityCodeElementId)
    errors.map(_.message) shouldBe Seq(commodityCodeErrorMessage)
  }

}
