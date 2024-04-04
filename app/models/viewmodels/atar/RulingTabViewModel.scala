/*
 * Copyright 2024 HM Revenue & Customs
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

package models
package viewmodels.atar

import java.time.Instant

case class RulingTabViewModel(
  caseReference: String,
  caseStatus: CaseStatus.Value,
  caseCreatedDate: Instant,
  caseHasExpiredRuling: Boolean,
  suggestedCommodityCode: Option[String],
  goodsDescription: String,
  bindingCommodityCode: Option[CommodityCode],
  decision: Option[Decision]
)

object RulingTabViewModel {
  def fromCase(cse: Case): RulingTabViewModel = RulingTabViewModel(
    caseReference          = cse.reference,
    caseStatus             = cse.status,
    caseCreatedDate        = cse.createdDate,
    caseHasExpiredRuling   = cse.hasExpiredRuling,
    suggestedCommodityCode = cse.application.asATAR.envisagedCommodityCode,
    goodsDescription       = cse.application.asATAR.goodDescription,
    // Commodity code expiry is not checked until we can integrate with the UK Global Tariff
    bindingCommodityCode = cse.decision.map(_.bindingCommodityCode).map(CommodityCode(_)),
    decision             = cse.decision
  )
}
