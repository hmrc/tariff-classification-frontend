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
                            attachments: Seq[String] = Seq.empty)

object DecisionForm {

  val form = Form(
    mapping(
      "bindingCommodityCode" -> text.verifying(emptyOr(validCommodityCode): _*),
      "goodsDescription" -> text,
      "methodSearch" -> text,
      "justification" -> text,
      "methodCommercialDenomination" -> text,
      "methodExclusion" -> text,
      "attachments" -> seq(text)
    )(DecisionFormData.apply)(DecisionFormData.unapply)
  )

  val mandatoryFieldsForm: Form[DecisionFormData] = Form(
    mapping(
      "bindingCommodityCode" -> text.verifying(emptyOr(validCommodityCode): _*),
      "goodsDescription" -> nonEmptyText,
      "methodSearch" -> nonEmptyText,
      "justification" -> nonEmptyText,
      "methodCommercialDenomination" -> text,
      "methodExclusion" -> text,
      "attachments" -> seq(text)
    )(DecisionFormData.apply)(DecisionFormData.unapply)
  )

  def mapFrom(d: Decision): Map[String, Seq[String]] = {
    Map(
      "bindingCommodityCode" -> Seq(d.bindingCommodityCode),
      "goodsDescription" -> Seq(d.goodsDescription),
      "methodSearch" -> Seq(d.methodSearch.getOrElse("")),
      "justification" -> Seq(d.justification),
      "methodCommercialDenomination" -> Seq(d.methodCommercialDenomination.getOrElse("")),
      "methodExclusion" -> Seq(d.methodExclusion.getOrElse("")),
      "attachments" -> Seq.empty
    )
  }
}


