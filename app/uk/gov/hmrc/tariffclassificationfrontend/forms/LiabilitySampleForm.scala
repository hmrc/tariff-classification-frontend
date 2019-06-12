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

import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, Forms, Mapping}
import uk.gov.hmrc.tariffclassificationfrontend.forms.FormConstraints.emptyOr
import uk.gov.hmrc.tariffclassificationfrontend.forms.mappings.FormMappings._
import uk.gov.hmrc.tariffclassificationfrontend.models.SampleSending
import uk.gov.hmrc.tariffclassificationfrontend.models.SampleSending.SampleSending

object LiabilitySampleForm {

  private def oneOf(values: SampleSending.ValueSet): Constraint[String] = Constraint("constraints.sample-return") {
    case s: String if SampleSending.values.exists(_.toString == s) => Valid
    case _ => Invalid(s"Must be one of [${values.toSeq.mkString(", ")}]")
  }

  private val mapping: Mapping[Option[SampleSending]] = Forms.mapping[Option[SampleSending], String](
    "return" -> fieldNonEmpty("error.empty.sending-liability-sample").verifying(emptyOr(oneOf(SampleSending.values)): _*)
  )(v => SampleSending.values.find(_.toString == v))(_.map(_.toString))

  val form: Form[Option[SampleSending]] = Form[Option[SampleSending]](mapping)

}