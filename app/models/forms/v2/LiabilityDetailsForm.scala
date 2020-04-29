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

import models.forms.FormConstraints.{dateMustBeInThePast, emptyOr}
import models.forms.FormDate
import models.forms.mappings.Constraints
import models.forms.mappings.FormMappings._
import models._
import play.api.data.Form
import play.api.data.Forms._

object LiabilityDetailsForm extends Constraints {

  private val numbersOnlyRegex = """^\d+$""".r
  private val btiRefRegex = """[0-9]{6,22}""".r

  def liabilityDetailsForm(existingLiability: Case): Form[Case] = Form[Case](
    mapping[Case, Option[Instant], String, Option[String],Option[String], Option[String],
      Option[String], Option[String], Option[String],Option[String], Option[String], Boolean,
      Option[Instant], Option[String], Option[String], Option[String], Option[String],
       String, Option[String], Option[String], Option[String],
      Option[Instant]](
      "entryDate" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))),
      "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
      //TODO make sure dont need validation
      "traderEmail" -> optional(text.verifying(emptyOr(validEmail("case.liability.error.trader.email")):_*)),
      "traderPhone" -> optional(text),
      "traderBuildingAndStreet" -> optional(text),
      "traderTownOrCity" -> optional(text),
      "traderCounty" -> optional(text),
      "traderPostcode" -> optional(text),
      "boardsFileNumber" -> optional(text),
      //TODO ^^
      "btiReference" -> optional(text.verifying(emptyOr(regexp(btiRefRegex, "case.v2.liability.c592.details_edit.bti_reference_error")): _*)),
      "repaymentClaim" -> boolean,
      "dateOfReceipt" -> optional(FormDate.date("case.liability.error.date-of-receipt")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))),
      "goodName" -> optional(text),
      "entryNumber" -> optional(
        text.verifying(emptyOr(regexp(numbersOnlyRegex, "case.liability.error.entry-number")): _*)
      ),
      "traderCommodityCode" -> optional(text),
      "officerCommodityCode" -> optional(text),
      "contactName" -> text,
      "contactEmail" -> optional(text.verifying(emptyOr(validEmail("case.liability.error.contact.email")):_*)),
      "contactPhone" -> optional(text),
      "dvrNumber" -> optional(
        text.verifying(emptyOr(regexp(numbersOnlyRegex, "case.liability.error.dvr-number")): _*)
      ),
      //TODO revisit .verifying logic
      "dateForRepayment" -> optional(FormDate.date("case.liability.error.date-of-repayment")
        .verifying(dateMustBeInThePast("case.liability.error.date-of-repayment.future")))
    )(form2Liability(existingLiability))(liability2Form)
  ).fillAndValidate(existingLiability)


  private def form2Liability(existingCase: Case): (
    Option[Instant], String, Option[String], Option[String], Option[String], Option[String], Option[String],
      Option[String],Option[String],Option[String],
    Boolean, Option[Instant], Option[String], Option[String], Option[String], Option[String], String, Option[String], Option[String],
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
        contact = Contact(contactName, contactEmail.getOrElse(""), contactPhone)
      ))
  }

  private def liability2Form(existingCase: Case): Option[(
    Option[Instant], String, Option[String],Option[String], Option[String],
      Option[String], Option[String], Option[String],Option[String], Option[String],
    Boolean, Option[Instant], Option[String], Option[String],
    Option[String], Option[String], String, Option[String], Option[String],
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
      Some(existingLiability.contact.email),
      existingLiability.contact.phone,
      existingLiability.repaymentClaim.flatMap(_.dvrNumber),
      existingLiability.repaymentClaim.flatMap(_.dateForRepayment)
    ))
  }

  //TODO: As part of the follow-up ticket regarding complete form validation, add tests
  def liabilityDetailsCompleteForm(existingLiability: Case): Form[Case] = Form[Case](
    mapping[Case, Option[Instant], String, Option[String], Option[String],
      Option[String], Option[String], Option[String],Option[String], Option[String],
      Option[String], Boolean, Option[Instant], Option[String], Option[String], Option[String],
      Option[String], String, Option[String], Option[String], Option[String], Option[Instant]](
      "entryDate" -> optional(FormDate.date("case.liability.error.entry-date")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future")))
        .verifying("error.required", _.isDefined),
      "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
      //TODO find what need to validate
      //TODO not emptyOr but it is required need to change as part of other ticket
      "traderEmail" -> optional(nonEmptyText.verifying(emptyOr(validEmail("case.liability.error.trader.email")):_*)),
      "traderPhone" -> optional(text),
      "traderBuildingAndStreet" -> optional(text),
      "traderTownOrCity" -> optional(text),
      "traderCounty" -> optional(text),
      "traderPostcode" -> optional(text),
      "boardsFileNumber" -> optional(text),
      //TODO take a look ^^
      "btiReference" -> optional(nonEmptyText),
      "repaymentClaim" -> boolean,
      "dateOfReceipt" -> optional(FormDate.date("case.liability.error.date-of-receipt")
        .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))),
      "goodName" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "entryNumber" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "traderCommodityCode" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "officerCommodityCode" -> optional(nonEmptyText).verifying("error.required", _.isDefined),
      "contactName" -> nonEmptyText,
      //TODO not emptyOr but it is required need to change as part of other ticket
      "contactEmail" -> optional(nonEmptyText.verifying(emptyOr(validEmail("case.liability.error.contact.email")):_*)),
      "contactPhone" -> optional(text).verifying("error.required", _.isDefined),
      "dvrNumber" -> optional(
        text.verifying(regexp(numbersOnlyRegex, "case.liability.error.dvr-number"))
      ),
      "dateForRepayment" -> optional(FormDate.date("case.liability.error.date-of-repayment")
        .verifying(dateMustBeInThePast("case.liability.error.date-of-repayment.future")))
        .verifying("error.required", _.isDefined)
    )(form2Liability(existingLiability))(liability2Form)
  ).fillAndValidate(existingLiability)
}