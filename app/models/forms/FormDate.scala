/*
 * Copyright 2025 HM Revenue & Customs
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

import models.forms.mappings.Mappings
import play.api.data.Forms.{mapping, of}
import play.api.data.format.Formats.booleanFormat
import play.api.data.format.Formatter
import play.api.data.{FormError, Forms, Mapping}

import java.time.{Instant, ZoneOffset}
import scala.util.Try

object FormDate extends Mappings {

  private def stripToNull(value: String): String = Option(value).map(_.trim).filterNot(_.isEmpty).orNull

  def date(error: String): Mapping[Instant] =
    localDate(error)
      .transform(
        date => date.atStartOfDay(ZoneOffset.UTC).toInstant,
        instant => Try(instant.atZone(ZoneOffset.UTC).toLocalDate).getOrElse(null)
      )

  def optionalDate(
    prefix: String = "expiryDate",
    error: String = "atar.editRuling.expiryDate"
  ): Mapping[Option[Instant]] = {
    val booleanField = "explicitEndDate"

    val optionalDateFormat: Formatter[Option[Instant]] = new Formatter[Option[Instant]] {
      def date: Mapping[Option[Instant]] =
        localDate(error)
          .transform(
            date => Some(date.atStartOfDay(ZoneOffset.UTC).toInstant),
            instant => Try(instant.map(_.atZone(ZoneOffset.UTC).toLocalDate).orNull).getOrElse(null)
          )

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Instant]] = {
        val prefix  = Option(stripToNull(key)).map(k => s"$k.").getOrElse("")
        val fullKey = s"$prefix$booleanField"

        of[Boolean].binder
          .bind(s"${Option(stripToNull(key)).map(k => s"$k.").getOrElse("")}$booleanField", data)
          .flatMap { explicitEndDate =>
            if (explicitEndDate) {
              date.withPrefix(prefix).bind(data)
            } else {
              Right(None)
            }
          }
      }

      override def unbind(key: String, value: Option[Instant]): Map[String, String] =
        date.withPrefix(prefix).unbind(value)
    }

    mapping(
      booleanField -> Forms.boolean,
      ""           -> of[Option[Instant]](optionalDateFormat)
    )(OptionalDateForm.apply)(OptionalDateForm.unapply)
      .transform(d => d.instant, d => OptionalDateForm(d.isEmpty, d))

  }

  private case class OptionalDateForm(explicitEndDate: Boolean, instant: Option[Instant])
}
