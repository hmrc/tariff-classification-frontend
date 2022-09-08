/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.{ZoneOffset, ZonedDateTime}
import models.ModelsBaseSpec
import play.api.data.Form

class FormDateTest extends ModelsBaseSpec {

  private val test         = Form(FormDate.date("invalid.date"))
  private val optionalTest = Form(FormDate.optionalDate("", "invalid.date"))

  val emptyStr = ""

  "Date type" should {

    "require fields" in {
      test.bindFromRequest(Map.empty).errors should have length 1
    }

    "disallow empty fields" in {
      assertInvalid(
        day   = emptyStr,
        month = emptyStr,
        year  = emptyStr,
        List("invalid.date.error.required.all")
      )
    }

    "disallow empty day" in {
      assertInvalid(day = emptyStr, month = "1", year = "2000", List("invalid.date.error.required.one"))
    }

    "disallow empty month" in {
      assertInvalid(day = "1", month = emptyStr, year = "2000", List("invalid.date.error.required.one"))
    }

    "disallow empty year" in {
      assertInvalid(day = "1", month = "1", year = emptyStr, List("invalid.date.error.required.one"))
    }

    "disallow empty month and year" in {
      assertInvalid(day = "1", month = emptyStr, year = emptyStr, List("invalid.date.error.required.two"))
    }

    "disallow empty day and month" in {
      assertInvalid(day = emptyStr, month = emptyStr, year = "2000", List("invalid.date.error.required.two"))
    }

    "disallow empty day and year" in {
      assertInvalid(day = emptyStr, month = "1", year = emptyStr, List("invalid.date.error.required.two"))
    }

    "verify invalid date" in {
      assertInvalid(day = "30", month = "02", year = "2019", List("invalid.date.error.invalid"))
    }

    "maps to data" in {
      test
        .bindFromRequest(
          Map(
            "day"   -> Seq("1"),
            "month" -> Seq("1"),
            "year"  -> Seq("2019")
          )
        )
        .get shouldBe ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
    }

    def assertInvalid(day: String, month: String, year: String, expected: List[String]) = {
      val messages = test
        .bindFromRequest(Map("day" -> Seq(day), "month" -> Seq(month), "year" -> Seq(year)))
        .errors
        .flatMap(_.messages)

      messages shouldBe expected
    }
  }

  "Optional Date type" should {

    "not require fields" in {
      optionalTest.bindFromRequest(Map.empty).errors should have length 0
      assertOptionalInvalid(day = emptyStr, month = emptyStr, year = emptyStr, List(), hasEndDate = false)
    }

    "disallow empty fields" in {
      assertOptionalInvalid(
        day   = emptyStr,
        month = emptyStr,
        year  = emptyStr,
        List("invalid.date.error.required.all")
      )
    }

    "disallow empty day" in {
      assertOptionalInvalid(day = emptyStr, month = "1", year = "2000", List("invalid.date.error.required.one"))
    }

    "disallow empty month" in {
      assertOptionalInvalid(day = "1", month = emptyStr, year = "2000", List("invalid.date.error.required.one"))
    }

    "disallow empty year" in {
      assertOptionalInvalid(day = "1", month = "1", year = emptyStr, List("invalid.date.error.required.one"))
    }

    "disallow empty month and year" in {
      assertOptionalInvalid(day = "1", month = emptyStr, year = emptyStr, List("invalid.date.error.required.two"))
    }

    "disallow empty day and month" in {
      assertOptionalInvalid(day = emptyStr, month = emptyStr, year = "2000", List("invalid.date.error.required.two"))
    }

    "disallow empty day and year" in {
      assertOptionalInvalid(day = emptyStr, month = "1", year = emptyStr, List("invalid.date.error.required.two"))
    }

    "verify invalid date" in {
      assertOptionalInvalid(day = "30", month = "02", year = "2019", List("invalid.date.error.invalid"))
    }

    "not verify invalid date when not required" in {
      assertOptionalInvalid(day               = "30", month = "02", year = "2019", List(), hasEndDate = false)
      assertOptionalInvalidWithoutEndDate(day = "30", month = "02", year = "2019", List())
    }

    def assertOptionalInvalid(
      day: String,
      month: String,
      year: String,
      expected: List[String],
      hasEndDate: Boolean = true
    ) = {
      val messages = optionalTest
        .bindFromRequest(
          Map(
            "day"             -> Seq(day),
            "month"           -> Seq(month),
            "year"            -> Seq(year),
            "explicitEndDate" -> Seq(String.valueOf(hasEndDate))
          )
        )
        .errors
        .flatMap(_.messages)

      messages shouldBe expected
    }

    def assertOptionalInvalidWithoutEndDate(day: String, month: String, year: String, expected: List[String]) = {
      val messages = optionalTest
        .bindFromRequest(Map("day" -> Seq(day), "month" -> Seq(month), "year" -> Seq(year)))
        .errors
        .flatMap(_.messages)

      messages shouldBe expected
    }
  }
}
