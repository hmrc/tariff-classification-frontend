/*
 * Copyright 2025 HM Revenue & Customs
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

import models.{Address, Case}

case class TraderContact(name: String, email: String, telephone: String, address: Address)

object TraderContact {
  def fromCase(c: Case): TraderContact = {
    val liabilityOrder = c.application.asLiabilityOrder
    TraderContact(
      liabilityOrder.traderName,
      liabilityOrder.traderContactDetails.flatMap(_.email).getOrElse(""),
      liabilityOrder.traderContactDetails.flatMap(_.phone).getOrElse(""),
      liabilityOrder.traderContactDetails.flatMap(_.address).getOrElse(Address("", "", None, None))
    )
  }
}
