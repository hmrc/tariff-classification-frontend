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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.Instant

import uk.gov.hmrc.tariffclassificationfrontend.models
import uk.gov.hmrc.tariffclassificationfrontend.models.ApplicationType.ApplicationType
import uk.gov.hmrc.tariffclassificationfrontend.models.ImportExport.ImportExport
import uk.gov.hmrc.tariffclassificationfrontend.models.LiabilityStatus.LiabilityStatus

sealed trait Application {
  val `type`: ApplicationType
  val contact: Contact

  def asBTI: BTIApplication = {
    this.asInstanceOf[BTIApplication]
  }

  def asLiabilityOrder: LiabilityOrder = {
    this.asInstanceOf[LiabilityOrder]
  }

  def isBTI: Boolean = {
    this.isInstanceOf[BTIApplication]
  }

  def isLiabilityOrder: Boolean = {
    this.isInstanceOf[LiabilityOrder]
  }

  def isLiveLiabilityOrder: Boolean = {
    isLiabilityOrder && asLiabilityOrder.status == LiabilityStatus.LIVE
  }

  def businessName: String = {
    `type` match {
      case ApplicationType.BTI => asBTI.holder.businessName
      case ApplicationType.LIABILITY_ORDER => asLiabilityOrder.traderName
    }
  }

  def goodsName: String = {
    `type` match {
      case ApplicationType.BTI => asBTI.goodName
      case ApplicationType.LIABILITY_ORDER => asLiabilityOrder.goodName.getOrElse("")
    }
  }

  def getType: String = {
    `type` match {
      case ApplicationType.BTI => "BTI"
      case ApplicationType.LIABILITY_ORDER => "Liability"
    }
  }
}

object ApplicationType extends Enumeration {
  type ApplicationType = Value
  val BTI, LIABILITY_ORDER = Value
}

case class BTIApplication
(
  holder: EORIDetails,
  override val contact: Contact,
  agent: Option[AgentDetails] = None,
  offline: Boolean,
  goodName: String,
  goodDescription: String,
  confidentialInformation: Option[String],
  importOrExport: Option[ImportExport] = None,
  otherInformation: Option[String],
  reissuedBTIReference: Option[String],
  relatedBTIReference: Option[String],
  knownLegalProceedings: Option[String],
  envisagedCommodityCode: Option[String],
  sampleToBeProvided: Boolean,
  sampleToBeReturned: Boolean
) extends Application {
  override val `type`: models.ApplicationType.Value = ApplicationType.BTI
}

case class AgentDetails
(
  eoriDetails: EORIDetails,
  letterOfAuthorisation: Option[Attachment]
)

case class LiabilityOrder
(
  override val contact: Contact,
  status: LiabilityStatus,
  traderName: String,
  goodName: Option[String] = None,
  entryDate: Option[Instant] = None,
  entryNumber: Option[String] = None,
  traderCommodityCode: Option[String] = None,
  officerCommodityCode: Option[String] = None
) extends Application {
  override val `type`: models.ApplicationType.Value = ApplicationType.LIABILITY_ORDER
}

object LiabilityStatus extends Enumeration {
  type LiabilityStatus = Value
  val LIVE, NON_LIVE = Value

  def format(liabilityStatus: LiabilityStatus) : String = {
    liabilityStatus match {
      case LIVE => "Live"
      case NON_LIVE => "Non-live"
    }
  }
}

case class EORIDetails
(
  eori: String,
  businessName: String,
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  postcode: String,
  country: String
)

case class Contact
(
  name: String,
  email: String,
  phone: Option[String] = None
)
