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

import models.SampleSend
import models.SampleSend.SampleSend
import models.forms.FormConstraints._
import models.forms.mappings.FormMappings._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, Forms, Mapping}

object SampleSendForm {

  private def oneOf(values: SampleSend.ValueSet): Constraint[String] = Constraint("constraints.sample-sender") {
    case s: String if SampleSend.values.exists(_.toString == s) => Valid
    case _                                                        => Invalid(s"Must be one of [${values.toSeq.mkString(", ")}]")
  }

  private val mapping: Mapping[Option[SampleSend]] = Forms.mapping[Option[SampleSend], String](
    "sample-sender" -> fieldNonEmpty("error.empty.sample.sender").verifying(emptyOr(oneOf(SampleSend.values)): _*)
  )(v => SampleSend.values.find(_.toString == v))(_.map(_.toString))

  val form: Form[Option[SampleSend]] = Form[Option[SampleSend]](mapping)

}
