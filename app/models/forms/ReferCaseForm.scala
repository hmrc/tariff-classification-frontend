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
import models.forms.mappings.FormMappings.textNonEmpty
import models.CaseReferral
import play.api.data.validation.Constraint
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import models.ReferralReason

object ReferCaseForm {
  val referentSpecified: Constraint[CaseReferral] = Constraint { referral =>
    val noReferent = referral.referManually.filterNot(_.isEmpty).isEmpty
    if (referral.referredTo.equalsIgnoreCase("Other") & noReferent)
      Invalid("Enter who you are referring this case to")
    else
      Valid
  }

  val reasonSpecified: Constraint[CaseReferral] = Constraint { referral =>
    if (referral.referredTo.equalsIgnoreCase("Applicant") && referral.reasons.isEmpty)
      Invalid("Select why you are referring this case")
    else
      Valid
  }

  lazy val form: Form[CaseReferral] = Form(
    mapping(
      "referredTo"    -> textNonEmpty("error.empty.refer.to"),
      "reasons"       -> list(text.transform[ReferralReason.Value](ReferralReason.withName, _.toString)),
      "note"          -> textNonEmpty("error.empty.refer.note"),
      "referManually" -> optional(text)
    )(CaseReferral.apply)(CaseReferral.unapply).verifying(
      referentSpecified,
      reasonSpecified
    )
  )
}
