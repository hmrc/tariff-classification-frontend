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

import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, Forms, Mapping}
import models.forms.FormConstraints._
import models.forms.mappings.FormMappings._
import models.SampleReturn
import models.SampleReturn.SampleReturn

object SampleReturnForm {

  private def oneOf(values: SampleReturn.ValueSet): Constraint[String] = Constraint("constraints.sample-return") {
    case s: String if SampleReturn.values.exists(_.toString == s) => Valid
    case _                                                        => Invalid(s"Must be one of [${values.toSeq.mkString(", ")}]")
  }

  private val mapping: Mapping[Option[SampleReturn]] = Forms.mapping[Option[SampleReturn], String](
    "return" -> fieldNonEmpty("error.empty.sample.return").verifying(emptyOr(oneOf(SampleReturn.values)): _*)
  )(v => SampleReturn.values.find(_.toString == v))(_.map(_.toString))

  val form: Form[Option[SampleReturn]] = Form[Option[SampleReturn]](mapping)

}
