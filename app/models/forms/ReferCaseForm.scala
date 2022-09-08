/*
 * Copyright 2022 HM Revenue & Customs
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

import models.forms.mappings.FormMappings.textNonEmpty
import models.{CaseReferral, ReferralReason}
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Mapping}

object ReferCaseForm {
  val referentSpecified: Formatter[Option[String]] = new Formatter[Option[String]] {
    def optionalMapping(key: String): Mapping[Option[String]] =
      single(key -> optional(text))

    def mandatoryMapping(key: String): Mapping[Option[String]] =
      single(
        key -> textNonEmpty("Enter who you are referring this case to")
          .transform[Option[String]](Some(_), _.get)
      )

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      data.get("referredTo") match {
        case Some(value) if value.equalsIgnoreCase("Other") => mandatoryMapping(key).bind(data)
        case _                                              => optionalMapping(key).bind(data)
      }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      optionalMapping(key).unbind(value)
  }

  val reasonSpecified: Formatter[List[ReferralReason.Value]] = new Formatter[List[ReferralReason.Value]] {
    def optionalMapping(key: String): Mapping[List[ReferralReason.Value]] =
      single(key -> list(text.transform[ReferralReason.Value](ReferralReason.withName, _.toString)))

    def mandatoryMapping(key: String): Mapping[List[ReferralReason.Value]] =
      single(
        key -> list(text.transform[ReferralReason.Value](ReferralReason.withName, _.toString))
          .verifying("Select why you are referring this case", _.nonEmpty)
      )

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], List[ReferralReason.Value]] =
      data.get("referredTo") match {
        case Some(value) if value.equalsIgnoreCase("Applicant") => mandatoryMapping(key).bind(data)
        case _                                                  => optionalMapping(key).bind(data)
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
