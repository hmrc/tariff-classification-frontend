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
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.tariffclassificationfrontend.models.{CancelReason, RulingCancellation}

object CancelRulingForm {

  private def oneOf(values: CancelReason.ValueSet): Constraint[String] = Constraint("constraints.cancel-reason") {
    case s: String if CancelReason.values.exists(_.toString == s) => Valid
    case _ => Invalid(s"Must be one of [${values.toSeq.mkString(", ")}]")
  }

  lazy val form: Form[RulingCancellation] = Form(mapping(
    "reason" -> text.verifying(oneOf(CancelReason.values)),
    "note" -> nonEmptyText
  )(RulingCancellation.apply)(RulingCancellation.unapply)
  )
}
