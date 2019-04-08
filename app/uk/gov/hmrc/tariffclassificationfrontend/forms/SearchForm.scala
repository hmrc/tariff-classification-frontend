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

import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.tariffclassificationfrontend.forms.FormConstraints._
import uk.gov.hmrc.tariffclassificationfrontend.models.PseudoCaseStatus.PseudoCaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{PseudoCaseStatus, Search}

import scala.util.Try

object SearchForm {

  private def textTransformingTo[A](reader: String => A, writer: A => String): Mapping[A] = {
    nonEmptyText
      .verifying("Invalid entry", s => Try(reader(s)).isSuccess)
      .transform[A](reader, writer)
  }

  val form: Form[Search] = Form(
    mapping(
      "trader_name" -> optional[String](text),
      "commodity_code" -> optional[String](text.verifying(emptyOr(numeric, minLength(2), maxLength(22)): _*)),
      "decision_details" -> optional[String](text),
      "status" -> optional[Set[PseudoCaseStatus]](set(textTransformingTo(PseudoCaseStatus.withName, _.toString))),
      "keyword" -> optional[Set[String]](set(text))
    )(Search.apply)(Search.unapply)
  )

  val formWithoutValidation: Form[Search] = Form(
    mapping(
      "trader_name" -> optional[String](text),
      "commodity_code" -> optional[String](text),
      "decision_details" -> optional[String](text),
      "status" -> optional[Set[PseudoCaseStatus]](set(textTransformingTo(PseudoCaseStatus.withName, _.toString))),
      "keyword" -> optional[Set[String]](set(text))
    )(Search.apply)(Search.unapply)
  )

}


