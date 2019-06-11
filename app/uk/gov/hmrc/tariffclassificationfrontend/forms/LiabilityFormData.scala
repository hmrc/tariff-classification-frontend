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

import java.time.{Instant, LocalDate, ZoneOffset}

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}

import scala.util.Try


case class LiabilityFormData(
                              //entryDate: Instant = Instant.now,
                              traderName: String = "",
                              goodName: String = "",
                              entryNumber: String = "",
                              traderCommodityCode: String = "",
                              officerCommodityCode: String = "",
                              contactName: String = "",
                              contactEmail: Option[String] = None,
                              contactPhone: String = "")

object LiabilityFormData {

  private type FormDate = (Int, Int, Int)

  private val formDateIsValid: FormDate => Boolean = {
    case (day, month, year) => Try(LocalDate.of(year, month, day)).isSuccess
  }

  val form: Form[LiabilityFormData] = Form[LiabilityFormData](
    mapping(
      "traderName" -> text,
      "goodName" -> text,
      "entryNumber" -> text,
      "traderCommodityCode" -> text,
      "officerCommodityCode" -> text,
      "contactName" -> text,
      "contactEmail" -> optional(email),
      "contactPhone" -> text
    )(LiabilityFormData.apply)(LiabilityFormData.unapply)
  )


}