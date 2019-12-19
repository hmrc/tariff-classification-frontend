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

import java.time.Clock

import javax.inject.{Inject, Singleton}
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.service.CommodityCodeService

@Singleton
class CommodityCodeConstraints @Inject()(commodityCodeService: CommodityCodeService, appConfig: AppConfig) {
  private implicit val clock: Clock = appConfig.clock

  val commodityCodeExistsInUKTradeTariff: Constraint[String] = Constraint("constraints.commodityCodeExists")({
    case s: String if s.isEmpty => Invalid("decision_form.error.bindingCommodityCode.required")
    case s: String if commodityCodeService.find(s).exists(_.isLive) =>
      Valid
    case s: String if commodityCodeService.find(s).exists(_.isExpired) =>
      Invalid("This commodity code has expired")
    case _: String =>
      Invalid("This commodity code is not a valid code in the UK Trade Tariff")
  })

}
