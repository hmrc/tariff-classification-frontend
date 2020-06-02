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

import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.validation.{Invalid, Valid}
import config.AppConfig
import models.ModelsBaseSpec
import service.CommodityCodeService

class CommodityCodeConstraintsSpec extends ModelsBaseSpec {

  "commodityCodeNonEmpty" should {
    "return invalid when there is no commodity code supplied" in {
      val commodityCodeConstraint = new CommodityCodeConstraints(mock[CommodityCodeService], mock[AppConfig])

      val result = commodityCodeConstraint.commodityCodeNonEmpty.apply("")

      result shouldBe Invalid("decision_form.error.bindingCommodityCode.required")
    }

    "return valid when there is no commodity code supplied" in {
      val commodityCodeConstraint = new CommodityCodeConstraints(mock[CommodityCodeService], mock[AppConfig])

      val result = commodityCodeConstraint.commodityCodeNonEmpty.apply("000000000")

      result shouldBe Valid
    }
  }
}
