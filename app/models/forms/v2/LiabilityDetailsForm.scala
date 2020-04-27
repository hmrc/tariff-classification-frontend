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

package models.forms.v2

import java.time.Instant

import models.forms.FormConstraints.dateMustBeInThePast
import models.forms.FormDate
import models.forms.mappings.FormMappings._
import models.{Address, Case, Contact, RepaymentClaim, TraderContactDetails}
import play.api.data.Form
import play.api.data.Forms._

object LiabilityDetailsForm {

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  def liabilityDetailsForm(existingLiability: Case): Form[Case] = Form[Case](
    mapping[Case, Option[Instant], String, Option[String],Option[String], Option[String],
      Option[String], Option[String], Option[String],Option[String], Option[String], Boolean,
      Option[Instant], Option[String],Option[String], Option[String], Option[String],
       String, String, Option[String], Option[String],
      Option[Instant]](
      "entryDate" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))),
      "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
      //TODO make sure dont need validation
      "traderEmail" -> optional(text),
      "traderPhone" -> optional(text),
      "traderBuildingAndStreet" -> optional(text),
      "traderTownOrCity" -> optional(text),
      "traderCounty" -> optional(text),
      "traderPostcode" -> optional(text),
      "boardsFileNumber" -> optional(text),
      //TODO ^^
      "btiReference" -> optional(text),
      "repaymentClaim" -> boolean,
      "dateOfReceipt" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))),
      "goodName" -> optional(text),
      "entryNumber" -> optional(text),
      "traderCommodityCode" -> optional(text),
      "officerCommodityCode" -> optional(text),
      "contactName" -> text,
      "contactEmail" -> text.verifying("case.liability.error.email", e => validEmailFormat(e)),
      "contactPhone" -> optional(text),
      "dvrNumber" -> optional(text),
      //TODO revisit .verifying logic
      "dateForRepayment" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future")))
    )(form2Liability(existingLiability))(liability2Form)
  ).fillAndValidate(existingLiability)

  private def validEmailFormat(email: String): Boolean = email.trim.isEmpty || emailRegex.findFirstMatchIn(email.trim).nonEmpty

  private def form2Liability(existingCase: Case): (
    Option[Instant], String, Option[String], Option[String], Option[String], Option[String], Option[String],
      Option[String],Option[String],Option[String],
    Boolean, Option[Instant], Option[String], Option[String], Option[String], Option[String], String, String, Option[String],
    Option[String], Option[Instant]) => Case = {
    case (entryDate, traderName, traderEmail,
    traderPhone, traderBuildingAndStreet, traderTownOrCity,
    traderCounty, traderPostcode, boardsFileNumber, btiReference, isRepaymentClaim,
    dateOfReceipt, goodName, entryNumber, traderCommodityCode, officerCommodityCode,
    contactName, contactEmail, contactPhone, dvrNumber, dateForRepayment) =>
      existingCase.copy(
        caseBoardsFileNumber = boardsFileNumber,
        application = existingCase.application.asLiabilityOrder.copy(
        traderName = traderName,
        traderContactDetails = Some(TraderContactDetails(traderEmail, traderPhone, Some(Address(traderBuildingAndStreet.getOrElse(""),traderTownOrCity.getOrElse(""),traderCounty, traderPostcode)))),
        btiReference = btiReference,
        repaymentClaim = if (isRepaymentClaim) Some(RepaymentClaim(dvrNumber = dvrNumber, dateForRepayment = dateForRepayment)) else None,
        dateOfReceipt = dateOfReceipt,
        goodName = goodName,
        entryDate = entryDate,
        entryNumber = entryNumber,
        traderCommodityCode = traderCommodityCode,
        officerCommodityCode = officerCommodityCode,
        contact = Contact(contactName, contactEmail, contactPhone)
      ))
  }

  private def liability2Form(existingCase: Case): Option[(
    Option[Instant], String, Option[String],Option[String], Option[String],
      Option[String], Option[String], Option[String],Option[String], Option[String],
    Boolean, Option[Instant], Option[String], Option[String],
    Option[String], Option[String], String, String, Option[String],
      Option[String], Option[Instant]
    )] = {
    val existingLiability = existingCase.application.asLiabilityOrder

    def buildingAndStreet(): Option[String] = Some(existingLiability.traderContactDetails.fold("")(_.address.fold("")(_.buildingAndStreet)))
    def townOrCity(): Option[String] = Some(existingLiability.traderContactDetails.fold("")(_.address.fold("")(_.townOrCity)))
    def county(): Option[String] = existingLiability.traderContactDetails.flatMap(_.address.flatMap(_.county))
    def postCode(): Option[String] = existingLiability.traderContactDetails.flatMap(_.address.flatMap(_.postCode))

    Some((
      existingLiability.entryDate,
      existingLiability.traderName,
      existingLiability.traderContactDetails.flatMap(_.email),
      existingLiability.traderContactDetails.flatMap(_.phone),
      buildingAndStreet(),
      townOrCity(),
      county(),
      postCode(),
      existingCase.caseBoardsFileNumber,
      existingLiability.btiReference,
      existingLiability.repaymentClaim.isDefined,
      existingLiability.dateOfReceipt,
      existingLiability.goodName,
      existingLiability.entryNumber,
      existingLiability.traderCommodityCode,
      existingLiability.officerCommodityCode,
      existingLiability.contact.name,
      existingLiability.contact.email,
      existingLiability.contact.phone,
      existingLiability.repaymentClaim.flatMap(_.dvrNumber),
      existingLiability.repaymentClaim.flatMap(_.dateForRepayment)
    ))
  }

  def liabilityDetailsCompleteForm(existingLiability: Case): Form[Case] = Form[Case](
    mapping[Case, Option[Instant], String, Option[String], Option[String],
      Option[String], Option[String], Option[String],Option[String], Option[String], Option[String], Boolean, Option[Instant],
      Option[String], Option[String], Option[String], Option[String], String,
      String, Option[String], Option[String], Option[Instant]](
      "entryDate" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future")))
        .verifying("error.required", _.isDefined),
      "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
      //TODO find what need to validate
      "traderEmail" -> optional(text),
      "traderPhone" -> optional(text),
      "traderBuildingAndStreet" -> optional(text),
      "traderTownOrCity" -> optional(text),
      "traderCounty" -> optional(text),
      "traderPostcode" -> optional(text),
      "boardsFileNumber" -> optional(text),
      //TODO take a look ^^
      "btiReference" -> optional(nonEmptyText),
      "repaymentClaim" -> boolean,
      "dateOfReceipt" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))),
      "goodName" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "entryNumber" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "traderCommodityCode" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "officerCommodityCode" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "contactName" -> nonEmptyText,
      "contactEmail" -> nonEmptyText.verifying("case.liability.error.email", e => validEmailFormat(e)),
      "contactPhone" -> optional(text).verifying("error.required", _.isDefined),
      "dvrNumber" -> optional(text),
      "dateForRepayment" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future")))
        .verifying("error.required", _.isDefined)
    )(form2Liability(existingLiability))(liability2Form)
  ).fillAndValidate(existingLiability)
}