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
import uk.gov.hmrc.tariffclassificationfrontend.models.Search

object SearchForm {

  val form: Form[Search] = Form(
    mapping(
      "trader_name" -> optional(text),
      "commodity_code" -> optional(text.verifying(emptyOr(numeric, minLength(2), maxLength(22)): _*)),
      "good_description" -> optional(text),
      "live_rulings_only" -> optional(boolean),
      "keyword" -> optional(set(text))
    )(Search.apply)(Search.unapply)
  )

  val formWithoutValidation: Form[Search] = Form(
    mapping(
      "trader_name" -> optional(text),
      "commodity_code" -> optional(text),
      "good_description" -> optional(text),
      "live_rulings_only" -> optional(boolean),
      "keyword" -> optional(set(text))
    )(Search.apply)(Search.unapply)
  )

}


