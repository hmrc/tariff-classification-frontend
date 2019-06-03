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
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseReferral, ReferralReason}

object ReferCaseForm {

  private def oneOf(values: ReferralReason.ValueSet): Constraint[String] = Constraint("constraints.referral-reason") {
    case s: String if ReferralReason.values.exists(_.toString == s) => Valid
    case _ => Invalid(s"Must be one of [${values.toSeq.mkString(", ")}]")
  }

  private def validateOther(referredTo : String, other: String): Boolean = {
    referredTo match {
      case "OTHER" if other.nonEmpty => true
      case "OTHER" => false
      case _ => true
    }
  }

  private def validateReason(referredTo: String, reason: String): Boolean = {
    referredTo match {
      case "APPLICANT" if reason.nonEmpty => true
      case "OTHER" => false
      case _ => true
    }
  }

  lazy val form: Form[CaseReferral] = Form(mapping(
    "referredTo" -> nonEmptyText,
    "reason" -> text,
    "note" -> nonEmptyText,
    "other" -> text
  )(CaseReferral.apply)(CaseReferral.unapply).verifying(
    "If OTHER is selected as referred to, the other field must be populated",
    constraint = fields =>
      fields match {
        case caseReferral => validateOther(caseReferral.referredTo, caseReferral.other)
      }
  ).verifying("If applicant is selected as referred to, there must be at least one reason",
    fields =>
      fields match {
        case caseReferral => validateReason(caseReferral.referredTo, caseReferral.reason)
      })
  )
}
