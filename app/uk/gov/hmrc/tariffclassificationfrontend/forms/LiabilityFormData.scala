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

import java.time.ZoneOffset._
import java.time.{Instant, LocalDate}

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.tariffclassificationfrontend.forms.mappings.FormMappings

import scala.util.Try


case class LiabilityFormData(entryDate: Option[Instant] = None,
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

  private val tupleToInstant: FormDate => Instant = {
    case (day, month, year) =>
      val min = LocalDate.of(year, month, day)
      min.atStartOfDay(UTC).toInstant
  }
  private val instantToTuple: Instant => FormDate = { date =>
    val offsetDate = date.atOffset(UTC).toLocalDate
    (offsetDate.getDayOfMonth, offsetDate.getMonthValue, offsetDate.getYear)
  }

  private val validDateFormat: FormDate => Boolean = {
    case (day, month, year) => Try(LocalDate.of(year, month, day)).isSuccess
  }

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private def validEmailFormat: Option[String] => Boolean = {
    case Some(e) if e.trim.isEmpty => true
    case Some(e) if emailRegex.findFirstMatchIn(e.trim).isEmpty => false
    case _ => true
  }

  val form: Form[LiabilityFormData] = Form(
    mapping(
      "entryDate" -> optional(tuple(
        "day" -> number,
        "month" -> number,
        "year" -> number)
        .verifying("case.liability.error.entry-date", validDateFormat)
        .transform(tupleToInstant, instantToTuple)),
      "traderName" -> FormMappings.textNonEmpty("case.liability.error.empty.trader-name"),
      "goodName" -> text,
      "entryNumber" -> text,
      "traderCommodityCode" -> text,
      "officerCommodityCode" -> text,
      "contactName" -> text,
      "contactEmail" -> optional(text).verifying("case.liability.error.email", validEmailFormat),
      "contactPhone" -> text
    )(LiabilityFormData.apply)(LiabilityFormData.unapply)
  )
}