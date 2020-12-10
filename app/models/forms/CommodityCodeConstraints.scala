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

import javax.inject.{Inject, Singleton}
import play.api.data.validation.{Constraint, Invalid, Valid}
import config.AppConfig
import service.CommodityCodeService
import models.forms.mappings.Constraints

@Singleton
class CommodityCodeConstraints @Inject() (commodityCodeService: CommodityCodeService, appConfig: AppConfig) extends Constraints {

  val commodityCodeNonEmpty: Constraint[String] =
    customNonEmpty("decision_form.error.bindingCommodityCode.required")

  val commodityCodeValid: Constraint[String] = Constraint("constraints.commoditycode")({
    case s: String if s.matches("[0-9]{6,22}") && (s.length % 2 == 0) => Valid
    case _: String =>
      Invalid("decision_form.error.bindingCommodityCode.valid")
  })
}
