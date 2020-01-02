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

package uk.gov.hmrc.tariffclassificationfrontend.forms

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
    myDate => Try(LocalDate.of(myDate.year.toInt, myDate.month.toInt, myDate.day.toInt)).isSuccess
  }

  def date(error: String = "invalid.date"): Mapping[Instant] =
    mapping("day" -> text, "month" -> text, "year" -> text)(DateForm.apply)(DateForm.unapply)
      .verifying(error, validDateFormat)
      .transform(formDate2Instant, instant2FormDate)

  private case class DateForm(day: String, month: String, year: String)
}
