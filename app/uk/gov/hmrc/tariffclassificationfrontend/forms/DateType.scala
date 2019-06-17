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

import java.time.ZoneOffset._
import java.time.{Clock, Instant, LocalDate}

import play.api.data.Forms._
import play.api.data.Mapping

import scala.util.Try

object DateType {

  private type FormDateStr = (String, String, String)

  private val formDate2Instant: FormDateStr => Instant = {
    case (day, month, year) =>
      LocalDate
        .of(year.toInt, month.toInt, day.toInt)
        .atStartOfDay(UTC)
        .toInstant
  }

  private val instant2FormDate: Instant => FormDateStr = { date =>
    val offsetDate = date.atOffset(UTC).toLocalDate
    (offsetDate.getDayOfMonth.toString, offsetDate.getMonthValue.toString, offsetDate.getYear.toString)
  }

  private val validDateFormat: FormDateStr => Boolean = {
    case (day, month, year) => Try(LocalDate.of(year.toInt, month.toInt, day.toInt)).isSuccess
  }

  private val dateMustBeInThePast: Instant => Boolean = _.isBefore(Instant.now(Clock.systemUTC))

  def date(error: String = "invalid.date"): Mapping[Instant] = tuple(
    "day" -> text,
    "month" -> text,
    "year" -> text
  ).verifying(error, validDateFormat)
    .transform(formDate2Instant, instant2FormDate)

  def pastDate(error: String): Mapping[Instant] = date(error)
    .verifying(s"$error.future", dateMustBeInThePast)

}
