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

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.tariffclassificationfrontend.forms.FormConstraints._
import uk.gov.hmrc.tariffclassificationfrontend.models.Decision

case class DecisionFormData(bindingCommodityCode: String = "",
                            goodsDescription: String = "",
                            methodSearch: String = "",
                            justification: String = "",
                            methodCommercialDenomination: String = "",
                            methodExclusion: String = "",
                            attachments: Seq[String] = Seq.empty,
                            explanation: String = "")

class DecisionForm @Inject()(commodityCodeConstraints: CommodityCodeConstraints) {

  val form: Form[DecisionFormData] = Form[DecisionFormData](
    mapping(
      "bindingCommodityCode" -> text.verifying(emptyOr(validCommodityCode, commodityCodeConstraints.commodityCodeExistsInUKTradeTariff): _*),
      "goodsDescription" -> text,
      "methodSearch" -> text,
      "justification" -> text,
      "methodCommercialDenomination" -> text,
      "methodExclusion" -> text,
      "attachments" -> seq(text),
      "explanation" -> text
    )(DecisionFormData.apply)(DecisionFormData.unapply)
  )

  val mandatoryFieldsForm: Form[DecisionFormData] = Form(
    mapping(
      "bindingCommodityCode" -> nonEmptyText,
      "goodsDescription" -> nonEmptyText,
      "methodSearch" -> nonEmptyText,
      "justification" -> nonEmptyText,
      "methodCommercialDenomination" -> text,
      "methodExclusion" -> text,
      "attachments" -> seq(text),
      "explanation" -> nonEmptyText
    )(DecisionFormData.apply)(DecisionFormData.unapply)
  )

  def bindFrom: Option[Decision] => Option[Form[DecisionFormData]] = { decision =>
    decision.map(mapFrom)
      .map(mandatoryFieldsForm.fillAndValidate)
  }

  private def mapFrom(d: Decision): DecisionFormData = {
    DecisionFormData(bindingCommodityCode = d.bindingCommodityCode,
      goodsDescription = d.goodsDescription,
      methodSearch = d.methodSearch.getOrElse(""),
      justification = d.justification,
      methodCommercialDenomination = d.methodCommercialDenomination.getOrElse(""),
      methodExclusion = d.methodExclusion.getOrElse(""),
      attachments = Seq.empty,
      explanation = d.explanation.getOrElse("")
    )
  }
}


