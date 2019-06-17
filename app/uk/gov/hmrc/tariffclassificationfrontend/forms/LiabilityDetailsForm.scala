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
import java.time.{Clock, Instant, LocalDate}

import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.tariffclassificationfrontend.forms.mappings.FormMappings._
import uk.gov.hmrc.tariffclassificationfrontend.models.{Contact, LiabilityOrder}

import scala.util.Try

object dateType {

  private type FormDateStr = (String, String, String)

  private val formDate2Instant: FormDateStr => Instant = {
    case (day, month, year) =>
      val min = LocalDate.of(year.toInt, month.toInt, day.toInt)
      min.atStartOfDay(UTC).toInstant
  }

  private val instant2FormDate: Instant => FormDateStr = { date =>
    val offsetDate = date.atOffset(UTC).toLocalDate
    (offsetDate.getDayOfMonth.toString, offsetDate.getMonthValue.toString, offsetDate.getYear.toString)
  }

  private val checkAllNumeric: FormDateStr => Boolean = {
    date => date.productIterator.count(s => Try(s.toString.toInt).isSuccess) == date.productIterator.size
  }

  private val validDateFormat: FormDateStr => Boolean = {
    case (day, month, year) if checkAllNumeric(day, month, year) => Try(LocalDate.of(year.toInt, month.toInt, day.toInt)).isSuccess
    case _ => false
  }

  private val dateMustBeInThePast: Instant => Boolean = _.isBefore(Instant.now(Clock.systemUTC))

  val pastDate: Mapping[Instant] = tuple(
    "day" -> text,
    "month" -> text,
    "year" -> text
  ).verifying("case.liability.error.entry-date", validDateFormat)
    .transform(formDate2Instant, instant2FormDate)
    .verifying("case.liability.error.future-date", dateMustBeInThePast)

}


object LiabilityDetailsForm {

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private def validEmailFormat(email: String): Boolean = email.trim.isEmpty || emailRegex.findFirstMatchIn(email.trim).nonEmpty

  private def form2Liability(existingLiability: LiabilityOrder): (Option[Instant], String, Option[String], Option[String], Option[String], Option[String], String, String, Option[String]) => LiabilityOrder = {
    case (entryDate, traderName, goodName, entryNumber, traderCommodityCode, officerCommodityCode, contactName, contactEmail, contactPhone) =>
      existingLiability.copy(
        traderName = traderName,
        goodName = goodName,
        entryDate = entryDate,
        entryNumber = entryNumber,
        traderCommodityCode = traderCommodityCode,
        officerCommodityCode = officerCommodityCode,
        contact = Contact(contactName, contactEmail, contactPhone)
      )
  }

  private def liability2Form(existingLiability: LiabilityOrder): Option[(Option[Instant], String, Option[String], Option[String], Option[String], Option[String], String, String, Option[String])] = {
    Some((
      existingLiability.entryDate,
      existingLiability.traderName,
      existingLiability.goodName,
      existingLiability.entryNumber,
      existingLiability.traderCommodityCode,
      existingLiability.officerCommodityCode,
      existingLiability.contact.name,
      existingLiability.contact.email,
      existingLiability.contact.phone
    ))
  }

  def liabilityDetailsForm(existingLiability: LiabilityOrder): Form[LiabilityOrder] = Form[LiabilityOrder](
    mapping[LiabilityOrder, Option[Instant], String, Option[String], Option[String], Option[String], Option[String], String, String, Option[String]](
      "entryDate" -> optional(dateType.pastDate),
      "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
      "goodName" -> optional(text),
      "entryNumber" -> optional(text),
      "traderCommodityCode" -> optional(text),
      "officerCommodityCode" -> optional(text),
      "contactName" -> text,
      "contactEmail" -> text.verifying("case.liability.error.email", e => validEmailFormat(e)),
      "contactPhone" -> optional(text)
    )(form2Liability(existingLiability))(liability2Form)
  ).fillAndValidate(existingLiability)

  def liabilityDetailsCompleteForm(existingLiability: LiabilityOrder): Form[LiabilityOrder] = Form[LiabilityOrder](
    mapping[LiabilityOrder, Option[Instant], String, Option[String], Option[String], Option[String], Option[String], String, String, Option[String]](
      "entryDate" -> optional(dateType.pastDate).verifying("error.required", _.isDefined),
      "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
      "goodName" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "entryNumber" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "traderCommodityCode" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "officerCommodityCode" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "contactName" -> nonEmptyText,
      "contactEmail" -> nonEmptyText.verifying("case.liability.error.email", e => validEmailFormat(e)),
      "contactPhone" -> optional(text).verifying("error.required", _.isDefined)
    )(form2Liability(existingLiability))(liability2Form)
  ).fillAndValidate(existingLiability)
}