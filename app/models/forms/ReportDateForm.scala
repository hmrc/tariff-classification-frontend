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

import java.time.{Instant, LocalDate, ZoneOffset}
import models.InstantRange
import models.forms.mappings.Mappings
import play.api.data.{Form, FormError, Mapping}
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.format.Formats._

import scala.util.Try

case class ReportDateFormData(
  specificDates: Boolean,
  dateRange: InstantRange
)

object ReportDateForm extends Mappings {
  private val specificDatesKey = "specificDates"
  private val dateRangeKey     = "dateRange"

  def date: Mapping[Instant] =
    localDate("reporting.startDate")
      .verifying(maxDate(LocalDate.now().plusDays(1), s"reporting.startDate.error.minimum", "day", "month", "year"))
      .transform(
        date => date.atStartOfDay(ZoneOffset.UTC).toInstant,
        instant => Try(instant.atZone(ZoneOffset.UTC).toLocalDate).getOrElse(null)
      )

  def endDateInclusive: Mapping[Instant] =
    localDate("reporting.endDate")
      .transform(
        date => date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant,
        instant => Try(instant.atZone(ZoneOffset.UTC).toLocalDate).getOrElse(null)
      )

  val optionalDateRangeFormat: Formatter[InstantRange] = new Formatter[InstantRange] {

    private def min(key: String) = s"$key.min"
    private def max(key: String) = s"$key.max"

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], InstantRange] =
      of[Boolean].binder.bind(specificDatesKey, data).flatMap { specificDates =>
        if (specificDates) {
          mapping(
            min(dateRangeKey) -> date,
            max(dateRangeKey) -> endDateInclusive
          )(InstantRange.apply)(InstantRange.unapply).bind(data)
        } else {
          Right(InstantRange.allTime)
        }
      }
    override def unbind(key: String, value: InstantRange): Map[String, String] =
      mapping(
        min(dateRangeKey) -> date,
        max(dateRangeKey) -> endDateInclusive
      )(InstantRange.apply)(InstantRange.unapply).unbind(value)
  }

  val form: Form[ReportDateFormData] = Form(
    mapping(
      specificDatesKey -> boolean("reporting.choose_date.required"),
      dateRangeKey + "_max_day" -> of[InstantRange](optionalDateRangeFormat).verifying(
        "reporting.choose_date.invalid_end_date",
        range => range.max.isAfter(range.min)
      )
    )(ReportDateFormData.apply)(ReportDateFormData.unapply)
  )
}
