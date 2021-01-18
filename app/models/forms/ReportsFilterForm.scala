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

package models.forms

import play.api.data.Form
import play.api.data.Forms._
import models.forms.FormConstraints._
import models.forms.FormUtils._
import models.PseudoCaseStatus.PseudoCaseStatus
import models._
object ReportsFilterForm {

  val form: Form[ReportsFilter] = Form(
    mapping(
      "searchTerm" -> optional[String](text),
      "status"     -> optional[Set[PseudoCaseStatus]](set(textTransformingTo(PseudoCaseStatus.withName, _.toString))),
      "caseType" -> optional[Set[ApplicationType]](
        set(textTransformingTo(ApplicationType.withName, _.name))
      ),
      "caseQueue" -> optional[Set[String]](set(text)),
      "officer"   -> optional[Set[String]](set(text.verifying(emptyOr(validCommodityCodeSearch): _*))),
      "kpi"       -> optional[Boolean](boolean)
    )(ReportsFilter.apply)(ReportsFilter.unapply)
  )

  /*

  case class ReportsFilter(
                            searchTerm: Option[String]             = None,
                            status: Option[Set[PseudoCaseStatus]]  = None,
                            caseType: Option[Set[ApplicationType]] = None,
                            caseQueue: Option[Set[Queue]]          = None,
                            officer: Option[Set[String]]           = None,
                            kpi: Option[Boolean]                   = None
                          )
   */
  /*
  val form: Form[Search] = Form(
    mapping(
      "trader_name"      -> optional[String](text),
      "commodity_code"   -> optional[String](text.verifying(emptyOr(validCommodityCodeSearch): _*)),
      "decision_details" -> optional[String](text),
      "status"           -> optional[Set[PseudoCaseStatus]](set(textTransformingTo(PseudoCaseStatus.withName, _.toString))),
      "application_type" -> optional[Set[ApplicationType]](
        set(textTransformingTo(ApplicationType.withName, _.name))
      ),
      "keyword" -> optional[Set[String]](set(text))
    )(Search.apply)(Search.unapply)
  )*/
  /*  val formWithoutValidation: Form[Search] = Form(
    mapping(
      "trader_name"      -> optional[String](text),
      "commodity_code"   -> optional[String](text),
      "decision_details" -> optional[String](text),
      "status"           -> optional[Set[PseudoCaseStatus]](set(textTransformingTo(PseudoCaseStatus.withName, _.toString))),
      "application_type" -> optional[Set[ApplicationType]](
        set(textTransformingTo(ApplicationType.withName, _.name))
      ),
      "keyword" -> optional[Set[String]](set(text))
    )(Search.apply)(Search.unapply)
  )*/

}
