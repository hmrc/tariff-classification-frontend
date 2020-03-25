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

import models.Case
import utils.Dates

case class C592ViewModel(entryNumber: String,
                         entryDate: String, btiCase: String,
                         repaymentClaim: Option[Boolean],
                         receiptDate: String,
                         itemName: String,
                         traderContact: TraderContact,
                         commodityCodeEnteredByTraderOrAgent: String,
                         commodityCodeSuggestedByOfficer: String,
                         portOrComplianceOfficerContact: PortOrComplianceOfficerContact,
                         dvrNumber: String,
                         dateForRepayment: String)

object C592ViewModel {

  //fields that need to be added to the Liability Model
  val NOT_YET_IMPLEMENTED = ""

  def fromCase(c: Case): C592ViewModel = {
    val liabilityOrder = c.application.asLiabilityOrder

    C592ViewModel(liabilityOrder.entryNumber.getOrElse(""),
      liabilityOrder.entryDate.map(Dates.format).getOrElse(""),
      NOT_YET_IMPLEMENTED,
      None,
      NOT_YET_IMPLEMENTED,
      liabilityOrder.goodName.getOrElse(""),
      TraderContact.fromCase(c),
      liabilityOrder.traderCommodityCode.getOrElse(""),
      liabilityOrder.officerCommodityCode.getOrElse(""),
      PortOrComplianceOfficerContact(liabilityOrder.contact.name,
        liabilityOrder.contact.email,
        liabilityOrder.contact.phone.getOrElse("")
      ),
      NOT_YET_IMPLEMENTED,
      NOT_YET_IMPLEMENTED
    )
  }

}
