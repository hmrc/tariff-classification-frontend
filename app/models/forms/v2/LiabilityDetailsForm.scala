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

package models.forms.v2

import config.AppConfig
import models._
import models.forms.FormConstraints._
import models.forms.mappings.FormMappings._
import models.forms.mappings.{Constraints, Mappings}
import models.forms.{CommodityCodeConstraints, FormDate}
import play.api.data.Forms._
import play.api.data.{Form, Forms, Mapping}

import java.time.Instant
import javax.inject.Inject

class LiabilityDetailsForm @Inject() (
  commodityCodeConstraints: CommodityCodeConstraints,
  appConfig: AppConfig
) extends Constraints
    with Mappings {

  def liabilityDetailsForm(existingLiability: Case): Form[Case] =
    Form[Case](
      mapping[
        Case,
        Option[Instant],
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
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Contact,
        Option[String],
        Option[String]
      ](
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
        "entryDate" -> optional(
          FormDate
            .date("case.liability.error.entry-date")
            .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))
            .verifying(
              dateLowerBound("case.liability.error.entry-date.year.lower.bound", appConfig.entryDateYearLowerBound)
            )
        ),
        "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
        // TODO make sure dont need validation
        "traderEmail" -> optional(Forms.text.verifying(emptyOr(validEmail("case.liability.error.trader.email")): _*)),
        "traderPhone" -> optional(Forms.text),
        "traderBuildingAndStreet" -> optional(Forms.text),
        "traderTownOrCity"        -> optional(Forms.text),
        "traderCounty"            -> optional(Forms.text),
        "traderPostcode" -> optional(Forms.text)
          .verifying(
            validPostcode("case.liability.error.postcode.valid"),
            optionalPostCodeMaxLength("case.liability.error.postcode.length")
          ),
        "boardsFileNumber" -> optional(Forms.text),
        "agentName"        -> optional(Forms.text),
        // TODO ^^
        "btiReference"   -> optional(Forms.text.verifying(emptyOr(btiReferenceIsCorrectFormat()): _*)),
        "repaymentClaim" -> Forms.boolean,
        "goodName"       -> optional(Forms.text).verifying(defined("case.liability.error.empty.good-name")),
        "entryNumber" -> optional(
          Forms.text.verifying(emptyOr(entryNumberIsNumbersAndLettersOnly()): _*)
        ),
        "traderCommodityCode" -> optional(
          Forms.text.verifying(
            emptyOr(
              commodityCodeConstraints.commodityCodeLengthValid,
              commodityCodeConstraints.commodityCodeNumbersValid,
              commodityCodeConstraints.commodityCodeEvenDigitsValid
            ): _*
          )
        ),
        "officerCommodityCode" -> optional(
          Forms.text.verifying(
            emptyOr(
              commodityCodeConstraints.commodityCodeLengthValid,
              commodityCodeConstraints.commodityCodeNumbersValid,
              commodityCodeConstraints.commodityCodeEvenDigitsValid
            ): _*
          )
        ),
        "contact" -> contactMapping,
        "port"    -> optional(Forms.text),
        "dvrNumber" -> optional(
          Forms.text.verifying(emptyOr(dvrNumberIsNumberAndLettersOnly()): _*)
        )
      )(form2Liability(existingLiability))(liability2Form)
    ).fillAndValidate(existingLiability)

  private def contactMapping: Mapping[Contact] =
    mapping(
      "contactName"  -> textNonEmpty("case.liability.error.compliance_officer.name"),
      "contactEmail" -> Forms.text.verifying(emptyOr(validEmail("case.liability.error.contact.email")): _*),
      "contactPhone" -> optional(Forms.text)
    )(Contact.apply)(o => Some(Tuple.fromProductTyped(o)))

  private def form2Liability(existingCase: Case): (
    Option[Instant],
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
    Option[String],
    Option[String],
    Option[String],
    Option[String],
    Contact,
    Option[String],
    Option[String]
  ) => Case = {
    case (
          dateOfReceipt,
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
          goodName,
          entryNumber,
          traderCommodityCode,
          officerCommodityCode,
          contact,
          port,
          dvrNumber
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
                  townOrCity = traderTownOrCity.getOrElse(""),
                  county = traderCounty,
                  postCode = traderPostcode
                )
              )
            )
          ),
          agentName = agentName,
          btiReference = btiReference,
          repaymentClaim =
            if (isRepaymentClaim) Some(RepaymentClaim(dvrNumber = dvrNumber, dateForRepayment = None))
            else None,
          dateOfReceipt = dateOfReceipt,
          goodName = goodName,
          entryDate = entryDate,
          entryNumber = entryNumber,
          traderCommodityCode = traderCommodityCode,
          officerCommodityCode = officerCommodityCode,
          contact = contact,
          port = port
        )
      )
  }

  private def liability2Form(existingCase: Case): Option[
    (
      Option[Instant],
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
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Contact,
      Option[String],
      Option[String]
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
        existingLiability.dateOfReceipt,
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
        existingLiability.goodName,
        existingLiability.entryNumber,
        existingLiability.traderCommodityCode,
        existingLiability.officerCommodityCode,
        existingLiability.contact,
        existingLiability.port,
        existingLiability.repaymentClaim.flatMap(_.dvrNumber)
      )
    )
  }

  // TODO: As part of the follow-up ticket regarding complete form validation, add tests
  def liabilityDetailsCompleteForm(existingLiability: Case): Form[Case] =
    Form[Case](
      mapping[
        Case,
        Option[Instant],
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
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Contact,
        Option[String],
        Option[String]
      ](
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
        "entryDate" -> optional(
          FormDate
            .date("case.liability.error.entry-date")
            .verifying(dateMustBeInThePast("case.liability.error.entry-date.future"))
            .verifying(
              dateLowerBound("case.liability.error.entry-date.year.lower.bound", appConfig.entryDateYearLowerBound)
            )
        ).verifying("Enter an entry date", _.isDefined),
        "traderName" -> textNonEmpty("case.liability.error.empty.trader-name"),
        // TODO find what need to validate
        // TODO not emptyOr but it is required need to change as part of other ticket
        "traderEmail" -> optional(
          Forms.text
            .verifying(customNonEmpty("Enter a trader email"))
            .verifying(emptyOr(validEmail("case.liability.error.trader.email")): _*)
        ),
        "traderPhone"             -> optional(Forms.text),
        "traderBuildingAndStreet" -> optional(Forms.text),
        "traderTownOrCity"        -> optional(Forms.text),
        "traderCounty"            -> optional(Forms.text),
        "traderPostcode" -> optional(Forms.text)
          .verifying(
            validPostcode("case.liability.error.postcode.valid"),
            optionalPostCodeMaxLength("case.liability.error.postcode.length")
          ),
        "boardsFileNumber" -> optional(Forms.text),
        "agentName"        -> optional(Forms.text),
        // TODO take a look ^^
        "btiReference"   -> optional(nonEmptyText),
        "repaymentClaim" -> Forms.boolean,
        "goodName"       -> optional(nonEmptyText).verifying("Enter the goods name", _.isDefined),
        "entryNumber"    -> optional(nonEmptyText).verifying("Enter an entry number", _.isDefined),
        "traderCommodityCode" -> optional(nonEmptyText)
          .verifying("Enter the commodity code from the trader", _.isDefined),
        "officerCommodityCode" -> optional(nonEmptyText)
          .verifying("Enter the code suggested by the officer", _.isDefined),
        "contact" -> contactMapping,
        "port"    -> optional(Forms.text),
        "dvrNumber" -> optional(
          Forms.text.verifying(dvrNumberIsNumberAndLettersOnly())
        ).verifying(
          "Enter a DVR number",
          f =>
            if (existingLiability.application.asLiabilityOrder.repaymentClaim.isDefined) f.isDefined
            else true
        )
      )(form2Liability(existingLiability))(liability2Form)
    ).fillAndValidate(existingLiability)
}
