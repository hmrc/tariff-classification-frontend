/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.{Form, Mapping}
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

case class DecisionForm(bindingCommodityCode: String = "",
                        goodsDescription: String = "",
                        methodSearch: String = "",
                        justification: String = "",
                        methodCommercialDenomination: String = "",
                        methodExclusion: String = "",
                        attachments: List[String] = List.empty) {
}

object DecisionForm extends FormConstraints {

  val form = Form(
    mapping(
      "bindingCommodityCode" -> commodityCodeCheck,
      "goodsDescription" -> nonEmptyText,
      "methodSearch" -> text,
      "justification" -> text,
      "methodCommercialDenomination" -> text,
      "methodExclusion" -> text,
      "attachments" -> list(text)
    )(DecisionForm.apply)(DecisionForm.unapply)
  )

}


trait FormConstraints {

  private val lengthRegex = """^[0-9]{12,24}$""".r

  val commodityCodeCheck: Mapping[String] = text.verifying(commodityCodeConstraint)

  def commodityCodeConstraint: Constraint[String] = Constraint[String]("constraint.commodity.code.length") { e =>
    lengthRegex.findFirstMatchIn(e)
      .map(_ => Valid)
      .getOrElse(Invalid(ValidationError("commodity code error")))
  }

}
