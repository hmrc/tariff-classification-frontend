/*
 * Copyright 2021 HM Revenue & Customs
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

import config.AppConfig
import models._
import models.forms.FormConstraints._
import models.forms.FormDate
import models.forms.mappings.Constraints
import models.forms.mappings.FormMappings._
import play.api.data.Form
import play.api.data.Forms._

object LiabilityDetailsForm extends Constraints {

  def liabilityDetailsForm(existingLiability: Case, appConfig: AppConfig): Form[Case] =
    Form[Case](
      mapping[
        Case,
        Option[Instant],
        String,
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Boolean,
        Option[Instant],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        String,
        Option[String],
        Option[String],
        Option[String],
        Option[Instant]
      ](
        "entryDate" -> optional(
          FormDate
            .date("case.liability.error.entry-date")
            .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))
            .verifying(
              dateLowerBound("case.liability.error.entry-date.year.lower.bound", appConfig.entryDateYearLowerBound)
            )
        ),
        "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
        //TODO make sure dont need validation
        "traderEmail"             -> optional(text.verifying(emptyOr(validEmail("case.liability.error.trader.email")): _*)),
        "traderPhone"             -> optional(text),
        "traderBuildingAndStreet" -> optional(text),
        "traderTownOrCity"        -> optional(text),
        "traderCounty"            -> optional(text),
        "traderPostcode"          -> optional(text),
        "boardsFileNumber"        -> optional(text),
        "agentName"              -> optional(text),
        //TODO ^^
        "btiReference"   -> optional(text.verifying(emptyOr(btiReferenceIsCorrectFormat()): _*)),
        "repaymentClaim" -> boolean,
        "dateOfReceipt" -> optional(
          FormDate
            .date("case.liability.error.date-of-receipt")
            .verifying(dateMustBeInThePast("case.liability.error.date-of-receipt.future"))
            .verifying(
              dateLowerBound(
                "case.liability.error.date-of-receipt.year.lower.bound",
                appConfig.dateOfReceiptYearLowerBound
              )
            )
        ),
        "goodName" -> optional(text).verifying(defined("case.liability.error.empty.good-name")),
        "entryNumber" -> optional(
          text.verifying(emptyOr(entryNumberIsNumbersAndLettersOnly()): _*)
        ),
        "traderCommodityCode"  -> optional(text),
        "officerCommodityCode" -> optional(text),
        "contactName"          -> textNonEmpty("case.liability.error.compliance_officer.name"),
        "contactEmail"         -> optional(text.verifying(emptyOr(validEmail("case.liability.error.contact.email")): _*)),
        "contactPhone"         -> optional(text),
        "dvrNumber" -> optional(
          text.verifying(emptyOr(dvrNumberIsNumberOnly()): _*)
        ),
        //TODO revisit .verifying logic
        "dateForRepayment" -> optional(
          FormDate
            .date("case.liability.error.date-of-repayment")
            .verifying(dateMustBeInThePast("case.liability.error.date-of-repayment.future"))
            .verifying(
              dateLowerBound(
                "case.liability.error.date-for-repayment.year.lower.bound",
                appConfig.dateForRepaymentYearLowerBound
              )
            )
        )
      )(form2Liability(existingLiability))(liability2Form)
    ).fillAndValidate(existingLiability)

  private def form2Liability(existingCase: Case): (
    Option[Instant],
    String,
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Boolean,
    Option[Instant],
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    String,
    Option[String],
    Option[String],
    Option[String],
    Option[Instant]
  ) => Case = {
    case (
        entryDate,
        traderName,
        traderEmail,
        traderPhone,
        traderBuildingAndStreet,
        traderTownOrCity,
        traderCounty,
        traderPostcode,
        boardsFileNumber,
        agentName,
        btiReference,
        isRepaymentClaim,
        dateOfReceipt,
        goodName,
        entryNumber,
        traderCommodityCode,
        officerCommodityCode,
        contactName,
        contactEmail,
        contactPhone,
        dvrNumber,
        dateForRepayment
        ) =>
      existingCase.copy(
        caseBoardsFileNumber = boardsFileNumber,
        application = existingCase.application.asLiabilityOrder.copy(
          traderName = traderName,
          traderContactDetails = Some(
            TraderContactDetails(
              email = traderEmail,
              phone = traderPhone,
              address = Some(
                Address(
                  buildingAndStreet = traderBuildingAndStreet.getOrElse(""),
                  townOrCity        = traderTownOrCity.getOrElse(""),
                  county            = traderCounty,
                  postCode          = traderPostcode
                )
              )
            )
          ),
          agentName = agentName,
          btiReference = btiReference,
          repaymentClaim =
            if (isRepaymentClaim) Some(RepaymentClaim(dvrNumber = dvrNumber, dateForRepayment = dateForRepayment))
            else None,
          dateOfReceipt        = dateOfReceipt,
          goodName             = goodName,
          entryDate            = entryDate,
          entryNumber          = entryNumber,
          traderCommodityCode  = traderCommodityCode,
          officerCommodityCode = officerCommodityCode,
          contact              = Contact(contactName, contactEmail.getOrElse(""), contactPhone)
        )
      )
  }

  private def liability2Form(existingCase: Case): Option[
    (
      Option[Instant],
      String,
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Boolean,
      Option[Instant],
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      String,
      Option[String],
      Option[String],
      Option[String],
      Option[Instant]
    )
  ] = {
    val existingLiability = existingCase.application.asLiabilityOrder

    def buildingAndStreet(): Option[String] =
      Some(existingLiability.traderContactDetails.fold("")(_.address.fold("")(_.buildingAndStreet)))

    def townOrCity(): Option[String] =
      Some(existingLiability.traderContactDetails.fold("")(_.address.fold("")(_.townOrCity)))

    def county(): Option[String] = existingLiability.traderContactDetails.flatMap(_.address.flatMap(_.county))

    def postCode(): Option[String] = existingLiability.traderContactDetails.flatMap(_.address.flatMap(_.postCode))

    Some(
      (
        existingLiability.entryDate,
        existingLiability.traderName,
        Some(existingLiability.traderContactDetails.map(_.email.getOrElse("")).getOrElse("")),
        Some(existingLiability.traderContactDetails.map(_.phone.getOrElse("")).getOrElse("")),
        buildingAndStreet(),
        townOrCity(),
        county(),
        postCode(),
        existingCase.caseBoardsFileNumber,
        existingLiability.agentName,
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
      )
    )
  }

  //TODO: As part of the follow-up ticket regarding complete form validation, add tests
  def liabilityDetailsCompleteForm(existingLiability: Case, appConfig: AppConfig): Form[Case] =
    Form[Case](
      mapping[
        Case,
        Option[Instant],
        String,
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Boolean,
        Option[Instant],
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        String,
        Option[String],
        Option[String],
        Option[String],
        Option[Instant]
      ](
        "entryDate" -> optional(
          FormDate
            .date("case.liability.error.entry-date")
            .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))
            .verifying(
              dateLowerBound("case.liability.error.entry-date.year.lower.bound", appConfig.entryDateYearLowerBound)
            )
        ).verifying("Enter an entry date", _.isDefined),
        "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
        //TODO find what need to validate
        //TODO not emptyOr but it is required need to change as part of other ticket
        "traderEmail"             -> optional(text.verifying(customNonEmpty("Enter a trader email"))
          .verifying(emptyOr(validEmail("case.liability.error.trader.email")): _*)),
        "traderPhone"             -> optional(text),
        "traderBuildingAndStreet" -> optional(text),
        "traderTownOrCity"        -> optional(text),
        "traderCounty"            -> optional(text),
        "traderPostcode"          -> optional(text),
        "boardsFileNumber"        -> optional(text),
        "agentName"              -> optional(text),
        //TODO take a look ^^
        "btiReference"   -> optional(nonEmptyText),
        "repaymentClaim" -> boolean,
        "dateOfReceipt" -> optional(
          FormDate
            .date("case.liability.error.date-of-receipt")
            .verifying(dateMustBeInThePast("case.liability.error.date-of-receipt.future"))
            .verifying(
              dateLowerBound(
                "case.liability.error.date-of-receipt.year.lower.bound",
                appConfig.dateOfReceiptYearLowerBound
              )
            )
        ),
        "goodName"             -> optional(nonEmptyText).verifying("Enter the goods name", _.isDefined),
        "entryNumber"          -> optional(nonEmptyText).verifying("Enter an entry number", _.isDefined),
        "traderCommodityCode"  -> optional(nonEmptyText).verifying("Enter the commodity code from the trader", _.isDefined),
        "officerCommodityCode" -> optional(nonEmptyText).verifying("Enter the code suggested by the officer", _.isDefined),
        "contactName"          -> textNonEmpty("case.liability.error.compliance_officer.name"),
        //TODO not emptyOr but it is required need to change as part of other ticket
        "contactEmail" -> optional(
          text.verifying(customNonEmpty("Enter a contact email"))
            .verifying(emptyOr(validEmail("case.liability.error.contact.email")): _*)
        ),
        "contactPhone" -> optional(text).verifying("Enter a contact telephone", _.isDefined),
        "dvrNumber" -> optional(
          text.verifying(dvrNumberIsNumberOnly())
        ),
        "dateForRepayment" -> optional(
          FormDate
            .date("case.liability.error.date-of-repayment")
            .verifying(dateMustBeInThePast("case.liability.error.date-of-repayment.future"))
            .verifying(
              dateLowerBound(
                "case.liability.error.date-for-repayment.year.lower.bound",
                appConfig.dateForRepaymentYearLowerBound
              )
            )
        ).verifying(
          "Enter a real date, for example 11/12/2020",
          f => {
            if(existingLiability.application.asLiabilityOrder.repaymentClaim.isDefined) f.isDefined
            else true
          }

        )
      )(form2Liability(existingLiability))(liability2Form)
    ).fillAndValidate(existingLiability)
}
