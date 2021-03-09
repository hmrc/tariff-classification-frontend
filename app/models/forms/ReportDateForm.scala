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

import java.time.{Instant, LocalDate, ZoneOffset}
import models.InstantRange
import models.forms.FormDate.DateForm
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.format.Formats._
import scala.util.Try

case class ReportDateFormData(
  specificDates: Boolean,
  dateRange: InstantRange
)

object ReportDateForm {
  private val specificDatesKey = "specificDates"
  private val dateRangeKey     = "dateRange"

  private val formDate2Instant: DateForm => Instant = { dateForm =>
    LocalDate
      .of(dateForm.year.toInt, dateForm.month.toInt, dateForm.day.toInt)
      .atStartOfDay(ZoneOffset.UTC)
      .toInstant
  }

  private val instant2FormDate: Instant => DateForm = { date =>
    if (date == Instant.MIN || date == Instant.MAX)
      DateForm("", "", "")
    else {
      val offsetDate = date.atOffset(ZoneOffset.UTC).toLocalDate
      DateForm(
        offsetDate.getDayOfMonth.toString,
        offsetDate.getMonthValue.toString,
        offsetDate.getYear.toString
      )
    }
  }

  def date =
    mapping(
      "day"   -> text,
      "month" -> text,
      "year"  -> text
    )(DateForm.apply)(DateForm.unapply)
      .verifying(
        "reporting.choose_date.invalid_date",
        date => Try(LocalDate.of(date.year.toInt, date.month.toInt, date.day.toInt)).isSuccess
      )
      .transform(formDate2Instant, instant2FormDate)

  val optionalDateRangeFormat: Formatter[InstantRange] = new Formatter[InstantRange] {

    private def min(key: String) = s"$key.min"
    private def max(key: String) = s"$key.max"

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], InstantRange] =
      of[Boolean].binder.bind(specificDatesKey, data).flatMap { specificDates =>
        if (specificDates)
          mapping(
            min(dateRangeKey) -> date,
            max(dateRangeKey) -> date
          )(InstantRange.apply)(InstantRange.unapply).bind(data)
        else
          Right(InstantRange.allTime)
      }
    override def unbind(key: String, value: InstantRange): Map[String, String] =
      mapping(
        min(dateRangeKey) -> date,
        max(dateRangeKey) -> date
      )(InstantRange.apply)(InstantRange.unapply).unbind(value)
  }

  val form: Form[ReportDateFormData] = Form(
    mapping(
      specificDatesKey -> optional(boolean)
        .verifying("reporting.choose_date.required", _.nonEmpty)
        .transform(_.get, Some[Boolean](_)),
      dateRangeKey -> of[InstantRange](optionalDateRangeFormat).verifying(
        "reporting.choose_date.invalid_end_date",
        range => range.max.isAfter(range.min)
      )
    )(ReportDateFormData.apply)(ReportDateFormData.unapply)
  )
}
