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

import play.api.data.Forms._
import play.api.data.validation._
import play.api.data.{Form, Mapping}

case class DecisionFormData(bindingCommodityCode: String = "",
                            goodsDescription: String = "",
                            methodSearch: String = "",
                            justification: String = "",
                            methodCommercialDenomination: String = "",
                            methodExclusion: String = "",
                            attachments: Seq[String] = Seq.empty) {
}

object DecisionForm extends FormConstraints {

  val form = Form(
    mapping(
      "bindingCommodityCode" -> verifyCommodityCode,
      "goodsDescription" -> text,
      "methodSearch" -> text,
      "justification" -> text,
      "methodCommercialDenomination" -> text,
      "methodExclusion" -> text,
      "attachments" -> seq(text)
    )(DecisionFormData.apply)(DecisionFormData.unapply)
  )
}

trait FormConstraints {


  //  Commodity code must be all numeric and contain between 10 and 22 digits

  private val commodityCodeRegex = """^([0-9]{6,22})|()$"""
  private val commodityCodeError = "Format must be numeric between 6 and 22 digits"

  val verifyCommodityCode: Mapping[String] = text.verifying(regexp(commodityCodeRegex, commodityCodeError))

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) => Valid
      case _ => Invalid(errorKey, regex)
    }
}