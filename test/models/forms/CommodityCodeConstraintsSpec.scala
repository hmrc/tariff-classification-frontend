/*
 * Copyright 2025 HM Revenue & Customs
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

import models.ModelsBaseSpec
import play.api.data.validation.{Invalid, Valid}

class CommodityCodeConstraintsSpec extends ModelsBaseSpec {

  "commodityCodeNonEmpty" should {
    "return invalid when there is no commodity code supplied" in {
      val commodityCodeConstraint = new CommodityCodeConstraints()

      val result = commodityCodeConstraint.commodityCodeNonEmpty.apply("")

      result shouldBe Invalid("decision_form.error.bindingCommodityCode.required")
    }

    "return valid when there is a commodity code supplied" in {
      val commodityCodeConstraint = new CommodityCodeConstraints()

      val result = commodityCodeConstraint.commodityCodeNonEmpty.apply("000000000")

      result shouldBe Valid
    }
  }

  "commodityCodeLengthValid" should {
    "return invalid when commodity code is not between 6 and 22 digits long" in {
      val commodityCodeConstraint = new CommodityCodeConstraints()

      val resultShort = commodityCodeConstraint.commodityCodeLengthValid.apply("12345")
      val resultLong  = commodityCodeConstraint.commodityCodeLengthValid.apply("12345678901234567890123")

      resultShort shouldBe Invalid("decision_form.error.bindingCommodityCode.valid.length")
      resultLong  shouldBe Invalid("decision_form.error.bindingCommodityCode.valid.length")
    }
  }

  "commodityCodeLengthValid" should {
    "return valid when commodity code is between 6 and 22 digits long" in {
      val commodityCodeConstraint = new CommodityCodeConstraints()

      val resultShort = commodityCodeConstraint.commodityCodeLengthValid.apply("123456")
      val resultLong  = commodityCodeConstraint.commodityCodeLengthValid.apply("1234567890123456789012")

      resultShort shouldBe Valid
      resultLong  shouldBe Valid
    }
  }

  "commodityCodeNumbersValid" should {
    "return invalid when commodity code is not a number" in {
      val commodityCodeConstraint = new CommodityCodeConstraints()

      val result = commodityCodeConstraint.commodityCodeNumbersValid.apply("12a45")

      result shouldBe Invalid("decision_form.error.bindingCommodityCode.valid.number")
    }
  }

  "commodityCodeNumbersValid" should {
    "return valid when commodity code is a number" in {
      val commodityCodeConstraint = new CommodityCodeConstraints()

      val result = commodityCodeConstraint.commodityCodeNumbersValid.apply("12345")

      result shouldBe Valid
    }
  }

  "commodityCodeEvenDigitsValid" should {
    "return invalid when commodity code length is not even" in {
      val commodityCodeConstraint = new CommodityCodeConstraints()

      val result = commodityCodeConstraint.commodityCodeEvenDigitsValid.apply("12345")

      result shouldBe Invalid("decision_form.error.bindingCommodityCode.valid.evenDigits")
    }
  }

  "commodityCodeEvenDigitsValid" should {
    "return valid when commodity code length is even" in {
      val commodityCodeConstraint = new CommodityCodeConstraints()

      val result = commodityCodeConstraint.commodityCodeEvenDigitsValid.apply("123456")

      result shouldBe Valid
    }
  }
}
