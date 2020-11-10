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

import java.time.{LocalDate, ZoneOffset}

import models.{InstantRange, ModelsBaseSpec}

class InstantRangeFormTest extends ModelsBaseSpec {

  "Instant Range Form" should {

    "disallow missing fields" in {
      InstantRangeForm.form
        .bindFromRequest(
          Map()
        )
        .errors shouldNot have length 0
    }

    "disallow empty fields" in {
      InstantRangeForm.form
        .bindFromRequest(
          Map(
            "min.day"   -> Seq(""),
            "min.month" -> Seq(""),
            "min.year"  -> Seq(""),
            "max.day"   -> Seq(""),
            "max.month" -> Seq(""),
            "max.year"  -> Seq("")
          )
        )
        .errors shouldNot have length 0
    }

    "verify invalid min date" in {
      InstantRangeForm.form
        .bindFromRequest(
          Map(
            "min.day"   -> Seq("32"),
            "min.month" -> Seq("13"),
            "min.year"  -> Seq("2019"),
            "max.day"   -> Seq("3"),
            "max.month" -> Seq("4"),
            "max.year"  -> Seq("2020")
          )
        )
        .errors shouldNot have length 0
    }

    "verify invalid max date" in {
      InstantRangeForm.form
        .bindFromRequest(
          Map(
            "min.day"   -> Seq("1"),
            "min.month" -> Seq("2"),
            "min.year"  -> Seq("2019"),
            "max.day"   -> Seq("32"),
            "max.month" -> Seq("13"),
            "max.year"  -> Seq("2020")
          )
        )
        .errors shouldNot have length 0
    }

    "maps to data" in {
      InstantRangeForm.form
        .bindFromRequest(
          Map(
            "min.day"   -> Seq("1"),
            "min.month" -> Seq("2"),
            "min.year"  -> Seq("2019"),
            "max.day"   -> Seq("3"),
            "max.month" -> Seq("4"),
            "max.year"  -> Seq("2020")
          )
        )
        .get shouldBe InstantRange(
        min = LocalDate.of(2019, 2, 1).atStartOfDay(ZoneOffset.UTC).toInstant,
        max = LocalDate.of(2020, 4, 3).atStartOfDay(ZoneOffset.UTC).toInstant
      )
    }

    "maps from data" in {
      InstantRangeForm.form
        .fill(
          InstantRange(
            min = LocalDate.of(2019, 2, 1).atStartOfDay(ZoneOffset.UTC).toInstant,
            max = LocalDate.of(2020, 4, 3).atStartOfDay(ZoneOffset.UTC).toInstant
          )
        )
        .data shouldBe Map(
        "min.day"   -> "1",
        "min.month" -> "2",
        "min.year"  -> "2019",
        "max.day"   -> "3",
        "max.month" -> "4",
        "max.year"  -> "2020"
      )
    }
  }
}
