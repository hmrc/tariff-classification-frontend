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

import play.api.data.Form
import play.api.data.Forms._
import models.forms.mappings.FormMappings._
import models.{Contact, LiabilityOrder, LiabilityStatus}
import FormUtils._
import models.LiabilityStatus.LiabilityStatus

object LiabilityForm {

  private val form2Liability: (String, String, LiabilityStatus) => LiabilityOrder = {
    case (itemName, traderName, status) =>
      LiabilityOrder(Contact("", "", None), status, traderName, Some(itemName), None, None)
  }

  private val liability2Form: LiabilityOrder => Option[(String, String, LiabilityStatus)] = liability =>
    Some((liability.goodName.getOrElse(""), liability.traderName, liability.status))

  val newLiabilityForm: Form[LiabilityOrder] = Form(
    mapping(
      "item-name" -> textNonEmpty("error.empty.item-name"),
      "trader-name" -> textNonEmpty("error.empty.trader-name"),
      "liability-status" -> textTransformingTo[LiabilityStatus](errorKey = "error.empty.liability-status",
        reader = LiabilityStatus.withName, writer = _.toString)
    )(form2Liability)(liability2Form)
  )

}
