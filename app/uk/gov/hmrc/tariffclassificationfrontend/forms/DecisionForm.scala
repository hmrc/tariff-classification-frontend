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

import play.api.data.{Form}
import play.api.data.Forms._

case class DecisionForm(bindingCommodityCode: String,
                        goodsDescription: String,
                        methodSearch: String,
                        justification: String,
                        methodCommercialDenomination: String,
                        methodExclusion: String,
                        attachments: Seq[String]) {
}

object DecisionForm {

  val form = Form(
    mapping(
      "bindingCommodityCode" -> text,
      "goodsDescription" -> text,
      "methodSearch" -> text,
      "justification" -> text,
      "methodCommercialDenomination" -> text,
      "methodExclusion" -> text,
      "attachments" -> seq(text)
    )(DecisionForm.apply)(DecisionForm.unapply)
  )
}
