/*
 * Copyright 2023 HM Revenue & Customs
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

import models.SampleStatus
import models.SampleStatus.SampleStatus
import models.forms.FormConstraints._
import models.forms.mappings.FormMappings._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, Forms, Mapping}

object SampleStatusForm {

  private def oneOf(values: SampleStatus.ValueSet): Constraint[String] = Constraint("constraints.sample-status") {
    case s: String if SampleStatus.values.exists(_.toString == s) => Valid
    case _                                                        => Invalid(s"Must be one of [${values.toSeq.mkString(", ")}]")
  }

  private val mapping: Mapping[Option[SampleStatus]] = Forms.mapping[Option[SampleStatus], String](
    "status" -> fieldNonEmpty("error.empty.sample.status").verifying(emptyOr(oneOf(SampleStatus.values)): _*)
  )(v => SampleStatus.values.find(_.toString == v))(_.map(_.toString))

  val form: Form[Option[SampleStatus]] = Form[Option[SampleStatus]](mapping)

}
