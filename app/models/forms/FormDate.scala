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

  private val formDate2OptionInstant: OptionalDateForm => Option[Instant] = { dateForm =>
    if(dateForm.explicitEndDate && !allFieldsEmpty(dateForm)){
    Some(LocalDate
      .of(dateForm.year.toInt, dateForm.month.toInt, dateForm.day.toInt)
      .atStartOfDay(UTC)
      .toInstant)}
    else None
  }

  private val optionInstant2FormDate: Option[Instant] => OptionalDateForm = {
    case Some(date) =>{
    val offsetDate = date.atOffset(UTC).toLocalDate
      OptionalDateForm(
      offsetDate.getDayOfMonth.toString,
      offsetDate.getMonthValue.toString,
      offsetDate.getYear.toString,
      true
    )}
    case None => OptionalDateForm("", "", "", false)
  }

  private val validDateFormat: DateForm => Boolean = { myDate =>
    if (validateDayInDate(myDate.day) && validateMonthInDate(myDate.month) && validateYearInDate(myDate.year)) {
      Try(LocalDate.of(myDate.year.toInt, myDate.month.toInt, myDate.day.toInt)).isSuccess
    } else {
      true
    }
  }

  private val validDateFormatOptonalDateForm: OptionalDateForm => Boolean = { myDate =>
    if (validateDayInDate(myDate.day) && validateMonthInDate(myDate.month) && validateYearInDate(myDate.year)) {
      Try(LocalDate.of(myDate.year.toInt, myDate.month.toInt, myDate.day.toInt)).isSuccess
    } else {
      true
    }
  }

  val validDateFormatOrEmpty: Constraint[OptionalDateForm] = Constraint("constraints.validDateFormat")({
    case d:OptionalDateForm if !d.explicitEndDate => Valid
    case d:OptionalDateForm if allFieldsEmpty(d) => Valid
    case d:OptionalDateForm if d.day.trim.isEmpty => Invalid("atar.editRuling.expiryDate.emptyDate.day")
    case d:OptionalDateForm if d.month.trim.isEmpty => Invalid("atar.editRuling.expiryDate.emptyDate.month")
    case d:OptionalDateForm if d.year.trim.isEmpty => Invalid("atar.editRuling.expiryDate.emptyDate.year")
    case d:OptionalDateForm if !validDateFormatOptonalDateForm(d) => Invalid("atar.editRuling.expiryDate.invalidFormat")
    case _ => Valid
  })

  private def validateDayInDate: String => Boolean   = !_.trim.isEmpty
  private def validateMonthInDate: String => Boolean = !_.trim.isEmpty
  private def validateYearInDate: String => Boolean  = date => !date.trim.isEmpty
  private def allFieldsEmpty: OptionalDateForm => Boolean = form => {
    form.day.trim.isEmpty && form.month.trim.isEmpty && form.year.trim.isEmpty
  }

  def date(error: String): Mapping[Instant] = {
    val emptyDay   = error + ".day"
    val emptyMonth = error + ".month"
    val emptyYear  = error + ".year"

    mapping (
      "day" -> text.verifying(emptyDay, validateDayInDate),
      "month" -> text.verifying(emptyMonth, validateMonthInDate),
      "year" -> text.verifying(emptyYear, validateYearInDate)
    )(DateForm.apply)(DateForm.unapply)
      .verifying(error, validDateFormat)
      .transform(formDate2Instant, instant2FormDate)
  }


  def optionalDate(): Mapping[Option[Instant]] = {

    val emptyDay = "atar.editRuling.expiryDate.emptyDate.day"
    val emptyMonth = "atar.editRuling.expiryDate.emptyDate.month"
    val emptyYear = "atar.editRuling.expiryDate.emptyDate.year"

    mapping(
    "day"             -> text.verifying(emptyDay, validateDayInDate),
    "month"           -> text.verifying(emptyMonth, validateMonthInDate),
    "year"            -> text.verifying(emptyYear, validateYearInDate),
    "explicitEndDate" -> boolean
  )(OptionalDateForm.apply)(OptionalDateForm.unapply)
    .verifying(validDateFormatOrEmpty)
    .transform(formDate2OptionInstant, optionInstant2FormDate)
  }

  case class DateForm(day: String, month: String, year: String)
  case class OptionalDateForm(day: String, month: String, year: String, explicitEndDate: Boolean)
}
