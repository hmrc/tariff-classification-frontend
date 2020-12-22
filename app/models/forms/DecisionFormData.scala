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

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import models.forms.FormConstraints._
import models.forms.mappings.Constraints
import models.Decision

case class DecisionFormData(
  bindingCommodityCode: String         = "",
  goodsDescription: String             = "",
  methodSearch: String                 = "",
  justification: String                = "",
  methodCommercialDenomination: String = "",
  methodExclusion: String              = "",
  attachments: Seq[String]             = Seq.empty,
  explanation: String                  = "",
  expiryDate: Option[Instant]          = None,
  explicitEndDate: Boolean             = false
)

class DecisionForm @Inject() (commodityCodeConstraints: CommodityCodeConstraints) extends Constraints {

  def btiForm(): Form[DecisionFormData] = {
   Form[DecisionFormData](
      mapping(
        "bindingCommodityCode" -> text.verifying(
          emptyOr(commodityCodeConstraints.commodityCodeValid): _*
        ),
        "goodsDescription"             -> text,
        "methodSearch"                 -> text,
        "justification"                -> text,
        "methodCommercialDenomination" -> text,
        "methodExclusion"              -> text,
        "attachments"                  -> seq(text),
        "explanation"                  -> text,
        "expiryDate"                   -> FormDate.optionalDate(),
        "explicitEndDate"              -> boolean
      )(DecisionFormData.apply)(DecisionFormData.unapply).verifying("atar.editRuling.expiryDate.emptyDate",
        formData => if(formData.explicitEndDate)formData.expiryDate.isDefined else true
      )
    )
  }

  val btiCompleteForm: Form[DecisionFormData] = Form[DecisionFormData](
    mapping(
      "bindingCommodityCode" -> text
        .verifying(
          StopOnFirstFail(
            commodityCodeConstraints.commodityCodeNonEmpty,
            commodityCodeConstraints.commodityCodeValid
          )
        ),
      "goodsDescription"             -> text.verifying(customNonEmpty("decision_form.error.itemDescription.required")),
      "methodSearch"                 -> text.verifying(customNonEmpty("decision_form.error.searchesPerformed.required")),
      "justification"                -> text.verifying(customNonEmpty("decision_form.error.legalJustification.required")),
      "methodCommercialDenomination" -> text,
      "methodExclusion"              -> text,
      "attachments"                  -> seq(text),
      "explanation"                  -> text.verifying(customNonEmpty("decision_form.error.decisionExplanation.required")),
      "expiryDate"                   -> FormDate.optionalDate(),
      "explicitEndDate"              -> boolean
    )(DecisionFormData.apply)(DecisionFormData.unapply).verifying("atar.editRuling.expiryDate.emptyDate",
      formData => if(formData.explicitEndDate) formData.expiryDate.isDefined else true
    )
  )



  def bindFrom: Option[Decision] => Option[Form[DecisionFormData]] =
    _.map(mapFrom)
      .map(btiCompleteForm.fillAndValidate)

  private def mapFrom(d: Decision): DecisionFormData =
    DecisionFormData(
      bindingCommodityCode         = d.bindingCommodityCode,
      goodsDescription             = d.goodsDescription,
      methodSearch                 = d.methodSearch.getOrElse(""),
      justification                = d.justification,
      methodCommercialDenomination = d.methodCommercialDenomination.getOrElse(""),
      methodExclusion              = d.methodExclusion.getOrElse(""),
      attachments                  = Seq.empty,
      explanation                  = d.explanation.getOrElse(""),
      expiryDate                   = d.effectiveEndDate,
      explicitEndDate              = d.effectiveEndDate.isDefined
    )

  def liabilityForm(existingDecision: Decision = Decision()): Form[Decision] =
    Form[Decision](
      mapping(
        "bindingCommodityCode" -> text.verifying(
          emptyOr(commodityCodeConstraints.commodityCodeValid): _*
        ),
        "goodsDescription" -> text,
        "methodSearch"     -> text,
        "justification"    -> text,
        "methodExclusion"  -> text
      )(liabilityForm2Decision(existingDecision))(decision2LiabilityForm)
    ).fillAndValidate(existingDecision)

  def liabilityCompleteForm(existingDecision: Decision = Decision()): Form[Decision] =
    Form[Decision](
      mapping(
        "bindingCommodityCode" -> nonEmptyText.verifying(commodityCodeConstraints.commodityCodeValid),
        "goodsDescription"     -> nonEmptyText,
        "methodSearch"         -> nonEmptyText,
        "justification"        -> nonEmptyText,
        "methodExclusion"      -> text
      )(liabilityForm2Decision(existingDecision))(decision2LiabilityForm)
    ).fillAndValidate(existingDecision)

  private def liabilityForm2Decision(existingDecision: Decision): (String, String, String, String, String) => Decision = {
    case (code, description, search, justification, exclusion) =>
      existingDecision.copy(
        bindingCommodityCode = code,
        goodsDescription     = description,
        justification        = justification,
        methodSearch         = Some(search).filter(_.nonEmpty),
        methodExclusion      = Some(exclusion).filter(_.nonEmpty)
      )
  }

  private def decision2LiabilityForm: Decision => Option[(String, String, String, String, String)] =
    d =>
      Some(
        (
          d.bindingCommodityCode,
          d.goodsDescription,
          d.methodSearch.getOrElse(""),
          d.justification,
          d.methodExclusion.getOrElse("")
        )
      )
}
