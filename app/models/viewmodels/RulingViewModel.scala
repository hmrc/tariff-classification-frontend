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

package models.viewmodels

import models.{Case, CaseStatus, Permission}

case class RulingViewModel(
                            commodityCodeEnteredByTraderOrAgent: String,
                            commodityCodeSuggestedByOfficer: String,
                            commodityCode: String,
                            itemDescription: String,
                            justification: String,
                            methodSearch: String,
                            methodExclusion: String,
                            showEditRuling: Boolean
                          )

object RulingViewModel {

  def fromCase(c: Case, permissions: Set[Permission] = Set.empty): RulingViewModel = {

    val bindingCommodityCode = c.decision.fold("")(_.bindingCommodityCode)
    val goodsDescription = c.decision.fold("")(_.goodsDescription)
    val decisionJustification = c.decision.fold("")(_.justification)
    val decisionMethodSearch = c.decision.fold("")(_.methodSearch.getOrElse(""))
    val decisionMethodExclusion = c.decision.fold("")(_.methodExclusion.getOrElse(""))

    val showEditRuling = c.status == CaseStatus.OPEN && permissions.contains(Permission.EDIT_RULING)

    RulingViewModel(
      commodityCodeEnteredByTraderOrAgent = c.application.asLiabilityOrder.traderCommodityCode.getOrElse(""),
      commodityCodeSuggestedByOfficer = c.application.asLiabilityOrder.officerCommodityCode.getOrElse(""),
      commodityCode = bindingCommodityCode,
      itemDescription = goodsDescription,
      justification = decisionJustification,
      methodSearch = decisionMethodSearch,
      methodExclusion = decisionMethodExclusion,
      showEditRuling
    )
  }
}
