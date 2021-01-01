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

package models.viewmodels

import models.Case
import utils.Dates

case class C592ViewModel(
  caseReference: String,
  entryNumber: String,
  entryDate: String,
  btiCase: String,
  repaymentClaim: String,
  receiptDate: String,
  itemName: String,
  traderContact: TraderContact,
  commodityCodeEnteredByTraderOrAgent: String,
  commodityCodeSuggestedByOfficer: String,
  portOrComplianceOfficerContact: PortOrComplianceOfficerContact,
  dvrNumber: String,
  dateForRepayment: String,
  caseBoardsFileNumber: Option[String],
  isRepaymentClaim: Boolean = false
)

object C592ViewModel {

  def fromCase(c: Case): C592ViewModel = {
    val liabilityOrder = c.application.asLiabilityOrder

    C592ViewModel(
      caseReference                       = c.reference,
      entryNumber                         = liabilityOrder.entryNumber.getOrElse(""),
      entryDate                           = liabilityOrder.entryDate.map(Dates.format).getOrElse(""),
      btiCase                             = liabilityOrder.btiReference.getOrElse(""),
      repaymentClaim                      = liabilityOrder.repaymentClaim.map(_ => "Yes").getOrElse("No"),
      receiptDate                         = liabilityOrder.dateOfReceipt.map(Dates.format).getOrElse(""),
      itemName                            = liabilityOrder.goodName.getOrElse(""),
      traderContact                       = TraderContact.fromCase(c),
      commodityCodeEnteredByTraderOrAgent = liabilityOrder.traderCommodityCode.getOrElse(""),
      commodityCodeSuggestedByOfficer     = liabilityOrder.officerCommodityCode.getOrElse(""),
      portOrComplianceOfficerContact = PortOrComplianceOfficerContact(
        liabilityOrder.contact.name,
        liabilityOrder.contact.email,
        liabilityOrder.contact.phone.getOrElse("")
      ),
      dvrNumber            = liabilityOrder.repaymentClaim.flatMap(_.dvrNumber).getOrElse(""),
      dateForRepayment     = liabilityOrder.repaymentClaim.flatMap(_.dateForRepayment).map(Dates.format).getOrElse(""),
      caseBoardsFileNumber = c.caseBoardsFileNumber,
      isRepaymentClaim     = liabilityOrder.repaymentClaim.isDefined
    )
  }

}
