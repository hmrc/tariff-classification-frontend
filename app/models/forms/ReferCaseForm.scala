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

import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.format.Formats._
import models.forms.mappings.FormMappings.textNonEmpty
import models.CaseReferral
import models.ReferralReason

object ReferCaseForm {
  val referentSpecified: Formatter[Option[String]] = new Formatter[Option[String]] {
    def optionalMapping(key: String) =
      single(key -> optional(text))

    def mandatoryMapping(key: String) =
      single(
        key -> textNonEmpty("Enter who you are referring this case to")
          .transform[Option[String]](Some(_), _.get)
      )

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      of[String].binder.bind("referredTo", data).flatMap { referredTo =>
        if (referredTo.equalsIgnoreCase("Other"))
          mandatoryMapping(key).bind(data)
        else
          optionalMapping(key).bind(data)
      }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      optionalMapping(key).unbind(value)
  }

  val reasonSpecified: Formatter[List[ReferralReason.Value]] = new Formatter[List[ReferralReason.Value]] {
    def optionalMapping(key: String) =
      single(key -> list(text.transform[ReferralReason.Value](ReferralReason.withName, _.toString)))

    def mandatoryMapping(key: String) =
      single(
        key -> list(text.transform[ReferralReason.Value](ReferralReason.withName, _.toString))
          .verifying("Select why you are referring this case", _.nonEmpty)
      )

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], List[ReferralReason.Value]] =
      of[String].binder.bind("referredTo", data).flatMap { referredTo =>
        if (referredTo.equalsIgnoreCase("Applicant"))
          mandatoryMapping(key).bind(data)
        else
          optionalMapping(key).bind(data)
      }

    override def unbind(key: String, value: List[ReferralReason.Value]): Map[String, String] =
      optionalMapping(key).unbind(value)
  }

  lazy val form: Form[CaseReferral] = Form(
    mapping(
      "referredTo"    -> textNonEmpty("error.empty.refer.to"),
      "reasons"       -> of[List[ReferralReason.Value]](reasonSpecified),
      "note"          -> textNonEmpty("error.empty.refer.note"),
      "referManually" -> of[Option[String]](referentSpecified)
    )(CaseReferral.apply)(CaseReferral.unapply)
  )
}
