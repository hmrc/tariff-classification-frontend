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

package models

import java.time.Instant

import models.LiabilityStatus.LiabilityStatus
import models.MiscCaseType.MiscCaseType

sealed trait Application {
  val `type`: ApplicationType
  val contact: Contact

  def asATAR: BTIApplication =
    this.asInstanceOf[BTIApplication]

  def asLiabilityOrder: LiabilityOrder =
    this.asInstanceOf[LiabilityOrder]

  def asCorrespondence: CorrespondenceApplication =
    this.asInstanceOf[CorrespondenceApplication]

  def asMisc: MiscApplication =
    this.asInstanceOf[MiscApplication]

  def isBTI: Boolean =
    this.isInstanceOf[BTIApplication]

  def isLiabilityOrder: Boolean =
    this.isInstanceOf[LiabilityOrder]

  def isLiveLiabilityOrder: Boolean =
    isLiabilityOrder && asLiabilityOrder.status == LiabilityStatus.LIVE

  def isCorrespondence: Boolean =
    this.isInstanceOf[CorrespondenceApplication]

  def isMisc: Boolean =
    this.isInstanceOf[MiscApplication]

  def businessName: String =
    `type` match {
      case ApplicationType.ATAR             => asATAR.holder.businessName
      case ApplicationType.LIABILITY => asLiabilityOrder.traderName
    }

  def goodsName: String =
    `type` match {
      case ApplicationType.ATAR             => asATAR.goodName
      case ApplicationType.LIABILITY => asLiabilityOrder.goodName.getOrElse("")
    }

  def getType: String =
    `type` match {
      case ApplicationType.ATAR             => "BTI"
      case ApplicationType.LIABILITY => "Liability"
      case ApplicationType.CORRESPONDENCE => "Correspondence"
      case ApplicationType.MISCELLANEOUS => "Misc"
    }
}

sealed abstract class ApplicationType(val name: String) extends Product with Serializable

object ApplicationType {
  val values = Set(ATAR, LIABILITY, CORRESPONDENCE, MISCELLANEOUS)

  def withName(name: String) = values.find(_.name == name).getOrElse(throw new NoSuchElementException)

  case object ATAR extends ApplicationType("BTI")
  case object LIABILITY extends ApplicationType("LIABILITY_ORDER")
  case object CORRESPONDENCE extends ApplicationType("CORRESPONDENCE")
  case object MISCELLANEOUS extends ApplicationType("MISCELLANEOUS")
}

case class BTIApplication(
  holder: EORIDetails,
  override val contact: Contact,
  agent: Option[AgentDetails] = None,
  offline: Boolean,
  goodName: String,
  goodDescription: String,
  confidentialInformation: Option[String],
  otherInformation: Option[String],
  reissuedBTIReference: Option[String],
  relatedBTIReference: Option[String] = None,
  relatedBTIReferences: List[String]  = Nil,
  knownLegalProceedings: Option[String],
  envisagedCommodityCode: Option[String],
  sampleToBeProvided: Boolean,
  sampleToBeReturned: Boolean,
  applicationPdf: Option[Attachment]
) extends Application {
  override val `type`: models.ApplicationType = ApplicationType.ATAR
}

case class AgentDetails(
  eoriDetails: EORIDetails,
  letterOfAuthorisation: Option[Attachment]
)

case class LiabilityOrder(
  override val contact: Contact,
  status: LiabilityStatus,
  traderName: String,
  goodName: Option[String]                           = None,
  entryDate: Option[Instant]                         = None,
  entryNumber: Option[String]                        = None,
  traderCommodityCode: Option[String]                = None,
  officerCommodityCode: Option[String]               = None,
  btiReference: Option[String]                       = None,
  repaymentClaim: Option[RepaymentClaim]             = None,
  dateOfReceipt: Option[Instant]                     = None,
  traderContactDetails: Option[TraderContactDetails] = None
) extends Application {
  override val `type`: models.ApplicationType = ApplicationType.LIABILITY
}

object LiabilityStatus extends Enumeration {
  type LiabilityStatus = Value
  val LIVE, NON_LIVE = Value

  def format(liabilityStatus: LiabilityStatus): String =
    liabilityStatus match {
      case LIVE     => "Live"
      case NON_LIVE => "Non-live"
    }
}

case class EORIDetails(
  eori: String,
  businessName: String,
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  postcode: String,
  country: String
)

case class Contact(
  name: String,
  email: String,
  phone: Option[String] = None
)

case class Message(
  name: String,
  date: Instant,
  message: String
                  )


case class CorrespondenceApplication(
                          correspondenceStarter: Option[String],
                          agentName: Option[String],
                          address: Address,
                          override val contact: Contact,
                          fax: Option[String] = None,
                          offline: Boolean,
                          summary: String,
                          detailedDescription: String,
                          relatedBTIReference: Option[String] = None,
                          relatedBTIReferences: List[String]  = Nil,
                          sampleToBeProvided: Boolean,
                          sampleToBeReturned: Boolean,
                          messagesLogged: List[Message]  = Nil
                                    ) extends Application {
  override val `type`: models.ApplicationType = ApplicationType.CORRESPONDENCE
}

case class MiscApplication(
                           override val contact: Contact,
                           offline: Boolean,
                           name: String,
                           contactName: Option[String],
                           caseType: MiscCaseType,
                           detailedDescription: Option[String],
                           sampleToBeProvided: Boolean,
                           sampleToBeReturned: Boolean,
                           messagesLogged: List[Message]  = Nil
                          ) extends Application {
  override val `type`: models.ApplicationType = ApplicationType.MISCELLANEOUS
}