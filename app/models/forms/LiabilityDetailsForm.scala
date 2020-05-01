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

import java.time.Instant

import play.api.data.Form
import play.api.data.Forms._
import models.forms.FormConstraints.dateMustBeInThePast
import models.forms.mappings.FormMappings._
import models.{Case, Contact}

object LiabilityDetailsForm {

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  def liabilityDetailsForm(existingLiability: Case): Form[Case] = Form[Case](
    mapping[Case, Option[Instant], String, Option[String], Option[String], Option[String], Option[String], String, String, Option[String]](
      "entryDate" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))),
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

  private def validEmailFormat(email: String): Boolean = email.trim.isEmpty || emailRegex.findFirstMatchIn(email.trim).nonEmpty

  private def form2Liability(existingCase: Case): (Option[Instant], String, Option[String], Option[String], Option[String], Option[String], String, String, Option[String]) => Case = {
    case (entryDate, traderName, goodName, entryNumber, traderCommodityCode, officerCommodityCode, contactName, contactEmail, contactPhone) =>
      existingCase.copy(application = existingCase.application.asLiabilityOrder.copy(
        traderName = traderName,
        goodName = goodName,
        entryDate = entryDate,
        entryNumber = entryNumber,
        traderCommodityCode = traderCommodityCode,
        officerCommodityCode = officerCommodityCode,
        contact = Contact(contactName, contactEmail, contactPhone)
      ))
  }

  private def liability2Form(existingCase: Case): Option[(Option[Instant], String, Option[String], Option[String], Option[String], Option[String], String, String, Option[String])] = {
    val existingLiability = existingCase.application.asLiabilityOrder

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

  def liabilityDetailsCompleteForm(existingLiability: Case): Form[Case] = Form[Case](
    mapping[Case, Option[Instant], String, Option[String], Option[String], Option[String], Option[String], String, String, Option[String]](
      "entryDate" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future")))
        .verifying("error.required", _.isDefined),
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