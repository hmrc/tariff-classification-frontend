/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import models.forms.mappings.Constraints
import play.api.data.validation.{Constraint, Invalid, Valid}

@Singleton
class CommodityCodeConstraints @Inject()() extends Constraints {

  val commodityCodeNonEmpty: Constraint[String] =
    customNonEmpty("decision_form.error.bindingCommodityCode.required")

  val commodityCodeLengthValid: Constraint[String] = Constraint("constraints.commoditycode")({
    case s: String if (s.length >= 6) && (s.length <= 22) => Valid
    case _: String =>
      Invalid("decision_form.error.bindingCommodityCode.valid.length")
  })

  val commodityCodeNumbersValid: Constraint[String] = Constraint("constraints.commoditycode")({
    case s: String if s.matches("[0-9]+") => Valid
    case _: String =>
      Invalid("decision_form.error.bindingCommodityCode.valid.number")
  })

  val commodityCodeEvenDigitsValid: Constraint[String] = Constraint("constraints.commoditycode")({
    case s: String if s.length % 2 == 0 => Valid
    case _: String =>
      Invalid("decision_form.error.bindingCommodityCode.valid.evenDigits")
  })
}
