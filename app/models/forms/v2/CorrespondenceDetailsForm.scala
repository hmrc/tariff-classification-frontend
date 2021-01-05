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

import config.AppConfig
import models._
import models.forms.mappings.Constraints
import models.forms.mappings.FormMappings._
import play.api.data.Form
import play.api.data.Forms._

object CorrespondenceDetailsForm extends Constraints {

  def correspondenceDetailsForm(existingCorrespondence: Case): Form[Case] =
    Form[Case](
      mapping[
        Case,
        String,
        String,
        Option[String]
      ](
        "summary"             -> textNonEmpty("can not be empty"),
        "detailedDescription" -> textNonEmpty("can not be empty"),
        "boardsFileNumber"    -> optional(text)
      )(form2Correspondence(existingCorrespondence))(correspondence2Form)
    ).fillAndValidate(existingCorrespondence)

  private def form2Correspondence(existingCase: Case): (
    String,
    String,
    Option[String]
  ) => Case = {
    case (
        summary,
        detailedDescription,
        boardsFileNumber
        ) =>
      existingCase.copy(
        caseBoardsFileNumber = boardsFileNumber,
        application = existingCase.application.asCorrespondence.copy(
          summary             = summary,
          detailedDescription = detailedDescription
        )
      )
  }

  private def correspondence2Form(existingCase: Case): Option[
    (
      String,
      String,
      Option[String]
    )
  ] = {
    val existingCorrespondence = existingCase.application.asCorrespondence

    Some(
      (
        existingCorrespondence.summary,
        existingCorrespondence.detailedDescription,
        existingCase.caseBoardsFileNumber
      )
    )
  }

  //TODO: COMPLETE CORRESPONDENCE FORM
/*  def correspondenceDetailsCompleteForm(existingLiability: Case, appConfig: AppConfig): Form[Case] =
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
      )(form2Correspondence(existingLiability))(correspondence2Form)
    ).fillAndValidate(existingLiability)*/
}
