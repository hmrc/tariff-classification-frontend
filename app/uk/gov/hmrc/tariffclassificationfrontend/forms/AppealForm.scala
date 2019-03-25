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
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, Forms, Mapping}
import uk.gov.hmrc.tariffclassificationfrontend.forms.FormConstraints._
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus.AppealStatus

object AppealForm {

  private def oneOf(values: AppealStatus.ValueSet): Constraint[String] = Constraint("constraints.appeal-status") {
    case s: String if AppealStatus.values.exists(_.toString == s) => Valid
    case _ => Invalid(s"Must be one of [${values.toSeq.mkString(", ")}]")
  }

  private val mapping: Mapping[Option[AppealStatus]] = Forms.mapping[Option[AppealStatus], String](
    "status" -> text.verifying(emptyOr(oneOf(AppealStatus.values)): _*)
  )(v => AppealStatus.values.find(_.toString == v))(_.map(_.toString))

  val form: Form[Option[AppealStatus]] = Form[Option[AppealStatus]](mapping)

}