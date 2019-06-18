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

import java.time.{ZoneOffset, ZonedDateTime}

import play.api.data.Form
import uk.gov.hmrc.play.test.UnitSpec

class FormDateTest extends UnitSpec {

  val test = Form(FormDate.date())

  val emptyStr = ""

  "Date type" should {

    "require fields" in {
      test.bindFromRequest(Map.empty).errors should have length 3
    }

    "disallow empty fields" in {
      assertInvalid(day = emptyStr, month = emptyStr, year = emptyStr)
    }

    "disallow empty day" in {
      assertInvalid(day = emptyStr, month = "1", year = "2000")
    }

    "disallow empty month" in {
      assertInvalid(day = "1", month = emptyStr, year = "2000")
    }

    "disallow empty year" in {
      assertInvalid(day = "1", month = "1", year = emptyStr)
    }

    "verify invalid date" in {
      assertInvalid(day = "30", month = "02", year = "2019")
    }

    "maps to data" in {
      test.bindFromRequest(
        Map(
          "day" -> Seq("1"),
          "month" -> Seq("1"),
          "year" -> Seq("2019")
        )
      ).get shouldBe ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
    }

    def assertInvalid(day: String, month: String, year: String) = {
      test.bindFromRequest(Map("day" -> Seq(day), "month" -> Seq(month), "year" -> Seq(year))).errors.flatMap(_.messages) shouldBe List("invalid.date")
    }
  }
}
