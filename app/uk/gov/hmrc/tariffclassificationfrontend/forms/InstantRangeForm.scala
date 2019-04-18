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

import java.time.{LocalDate, ZoneOffset}

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.tariffclassificationfrontend.models.InstantRange

import scala.util.Try

object InstantRangeForm {

  private type FormDate = (Int, Int, Int)

  private val mappingFromFormToFilter: (FormDate, FormDate) => InstantRange = {
    case ((minDay, minMonth, minYear), (maxDay, maxMonth, maxYear)) =>
      val min = LocalDate.of(minYear, minMonth, minDay)
      val max = LocalDate.of(maxYear, maxMonth, maxDay)
      InstantRange(
        min = min.atStartOfDay(ZoneOffset.UTC).toInstant,
        max = max.atStartOfDay(ZoneOffset.UTC).toInstant
      )
  }

  private val mappingFromFilterToForm: InstantRange => Option[(FormDate, FormDate)] = { range =>
    val min = range.min.atOffset(ZoneOffset.UTC).toLocalDate
    val max = range.max.atOffset(ZoneOffset.UTC).toLocalDate
    val formMinimum: FormDate = (min.getDayOfMonth, min.getMonthValue, min.getYear)
    val formMaximum: FormDate = (max.getDayOfMonth, max.getMonthValue, max.getYear)
    Some((formMinimum, formMaximum))
  }

  private val formDateIsValid: FormDate => Boolean = {
    case (day, month, year) => Try(LocalDate.of(year, month, day)).isSuccess
  }

  val form: Form[InstantRange] = Form(
    mapping(
      "min" -> tuple(
        "day" -> number,
        "month" -> number,
        "year" -> number
      ).verifying("Date must be valid", formDateIsValid),

      "max" -> tuple(
        "day" -> number,
        "month" -> number,
        "year" -> number
      ).verifying("Date must be valid", formDateIsValid)
    )(mappingFromFormToFilter)(mappingFromFilterToForm)
  )
}
