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
import play.api.data.validation.{Constraint, Invalid, Valid}

import scala.util.Try

object FormDate {

  private val formDate2Instant: DateForm => Instant = { dateForm =>
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

  private val formDate2OptionInstant: DateForm => Option[Instant] = { dateForm =>
    if(!allFieldsEmpty(dateForm)){
    Some(LocalDate
      .of(dateForm.year.toInt, dateForm.month.toInt, dateForm.day.toInt)
      .atStartOfDay(UTC)
      .toInstant)}
    else None
  }

  private val optionInstant2FormDate: Option[Instant] => DateForm = {
    case Some(date) =>{
    val offsetDate = date.atOffset(UTC).toLocalDate
    DateForm(
      offsetDate.getDayOfMonth.toString,
      offsetDate.getMonthValue.toString,
      offsetDate.getYear.toString
    )}
    case None => DateForm("", "", "")
  }

  private val validDateFormat: DateForm => Boolean = { myDate =>
      Try(LocalDate.of(myDate.year.toInt, myDate.month.toInt, myDate.day.toInt)).isSuccess
  }

  val validDateFormatOrEmpty: Constraint[DateForm] = Constraint("constraints.validDateFormat")({
    case d:DateForm if allFieldsEmpty(d) => Valid
    case d:DateForm if !validDateFormat(d) => Invalid("atar.editRuling.expiryDate.invalidFormat")
    case d:DateForm if d.day.trim.isEmpty => Invalid("atar.editRuling.expiryDate.emptyDate.day")
    case d:DateForm if d.month.trim.isEmpty => Invalid("atar.editRuling.expiryDate.emptyDate.month")
    case d:DateForm if d.year.trim.isEmpty => Invalid("atar.editRuling.expiryDate.emptyDate.year")
    case _ => Valid
  })

  private def validateDayInDate: DateForm => Boolean   = !_.day.trim.isEmpty
  private def validateMonthInDate: DateForm => Boolean = !_.month.trim.isEmpty
  private def validateYearInDate: DateForm => Boolean  = date => !date.year.trim.isEmpty
 private def allFieldsEmpty: DateForm => Boolean = form => {
    form.day.trim.isEmpty && form.month.trim.isEmpty && form.year.trim.isEmpty
  }

  def date(error: String): Mapping[Instant] = {
    val emptyDay   = error + ".day"
    val emptyMonth = error + ".month"
    val emptyYear  = error + ".year"

    mapping("day" -> text, "month" -> text, "year" -> text)(DateForm.apply)(DateForm.unapply)
      .verifying(emptyDay, validateDayInDate)
      .verifying(emptyMonth, validateMonthInDate)
      .verifying(emptyYear, validateYearInDate)
      .verifying(error, validDateFormat)
      .transform(formDate2Instant, instant2FormDate)
  }


  def optionalDate(): Mapping[Option[Instant]] = {
    mapping("day" -> text, "month" -> text, "year" -> text)(DateForm.apply)(DateForm.unapply)
      .verifying(validDateFormatOrEmpty)
      .transform(formDate2OptionInstant, optionInstant2FormDate)
  }

  case class DateForm(day: String, month: String, year: String)
}
