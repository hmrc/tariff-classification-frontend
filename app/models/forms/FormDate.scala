/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.ZoneOffset._
import java.time.{Instant, LocalDate}

import play.api.data.Forms._
import play.api.data.Mapping

import scala.util.Try

object FormDate {

  private val formDate2Instant: DateForm => Instant = {
    dateForm =>
      LocalDate
        .of(dateForm.year.toInt, dateForm.month.toInt, dateForm.day.toInt)
        .atStartOfDay(UTC)
        .toInstant
  }

  private val instant2FormDate: Instant => DateForm = { date =>
    val offsetDate = date.atOffset(UTC).toLocalDate
    DateForm(
      offsetDate.getDayOfMonth.toString,
      offsetDate.getMonthValue.toString,
      offsetDate.getYear.toString
    )
  }

  private val validDateFormat: DateForm => Boolean = {
    myDate => if (validateDayInDate(myDate) && validateMonthInDate(myDate) && validateYearInDate(myDate)) {
      Try(LocalDate.of(myDate.year.toInt, myDate.month.toInt, myDate.day.toInt)).isSuccess
    } else {
      true
    }
  }

  private def validateDayInDate: DateForm => Boolean = !_.day.trim.isEmpty
  private def validateMonthInDate: DateForm => Boolean = !_.month.trim.isEmpty
  private def validateYearInDate: DateForm => Boolean = date => !date.year.trim.isEmpty

  def date(error: String): Mapping[Instant] = {
    val emptyDay = error + ".day"
    val emptyMonth = error + ".month"
    val emptyYear = error + ".year"

    mapping("day" -> text, "month" -> text, "year" -> text)(DateForm.apply)(DateForm.unapply)
      .verifying(emptyDay, validateDayInDate)
      .verifying(emptyMonth, validateMonthInDate)
      .verifying(emptyYear, validateYearInDate)
      .verifying(error, validDateFormat)
      .transform(formDate2Instant, instant2FormDate)
  }

  private case class DateForm(day: String, month: String, year: String)
}
