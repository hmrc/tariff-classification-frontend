/*
 * Copyright 2018 HM Revenue & Customs
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

  val decisionForm: Form[DecisionFormData] = DecisionForm.form

  "bindingCommodityCode on Decision form constrains" should {

    val bindingCommodityCode = "bindingCommodityCode"

    "empty binding commodity code must be valid " in {
      decisionForm.bind(commodityCodeJsValue(""))
        .errors(bindingCommodityCode).size shouldBe 0
    }

    "numeric value between 6 and 22 digits must be valid " in {
      decisionForm.bind(commodityCodeJsValue("1234567890"))
        .errors(bindingCommodityCode).size shouldBe 0
    }

    "numeric value of 6 digits must be valid " in {
      decisionForm.bind(commodityCodeJsValue("123456"))
        .errors(bindingCommodityCode).size shouldBe 0
    }

    "numeric value of 22 digits must be valid " in {
      decisionForm.bind(commodityCodeJsValue("1234567891234567890000"))
        .errors(bindingCommodityCode).size shouldBe 0
    }

    "numeric value of 23 digits must be invalid  " in {
      val errors = decisionForm.bind(commodityCodeJsValue("12345678901234567890123"))
        .errors(bindingCommodityCode)

      errors.map(_.message) should contain only "Format must be empty or numeric between 6 and 22 digits"
    }

    "invalid numeric on binding commodity code return message error " in {
      val errors = decisionForm.bind(commodityCodeJsValue("12345"))
        .errors(bindingCommodityCode)

      errors.map(_.message) should contain only "Format must be empty or numeric between 6 and 22 digits"
    }

    "invalid alpha characters on binding commodity code return message error " in {
      val errors = decisionForm.bind(commodityCodeJsValue("12345Q"))
        .errors(bindingCommodityCode)

      errors.map(_.message) should contain only "Format must be empty or numeric between 6 and 22 digits"
    }

    "invalid special characters on binding commodity code return message error " in {
      val errors = decisionForm.bind(commodityCodeJsValue("12345!"))
        .errors(bindingCommodityCode)

      errors.map(_.message) should contain only "Format must be empty or numeric between 6 and 22 digits"
    }

    def commodityCodeJsValue(value: String): JsValue = {
      JsObject(Seq(bindingCommodityCode -> JsString(value)))
    }
  }
}