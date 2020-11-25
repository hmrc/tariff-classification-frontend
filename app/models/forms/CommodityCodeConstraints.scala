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

import java.time.Clock

import javax.inject.{Inject, Singleton}
import play.api.data.validation.Constraint
import config.AppConfig
import service.CommodityCodeService
import models.forms.mappings.Constraints

@Singleton
class CommodityCodeConstraints @Inject() (commodityCodeService: CommodityCodeService, appConfig: AppConfig) extends Constraints {
  private implicit val clock: Clock = appConfig.clock

  val commodityCodeNonEmpty: Constraint[String] =
    customNonEmpty("decision_form.error.bindingCommodityCode.required")

  val commodityCodeNumeric: Constraint[String] =
    regexp(FormConstraints.numbersOnlyRegex, "decision_form.error.bindingCommodityCode.numeric")
}
