/*
 * Copyright 2025 HM Revenue & Customs
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

package models.forms.mappings

import models.ModelsBaseSpec
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.data.validation.{Invalid, Valid}

import java.time.{Instant, LocalDate, ZoneOffset}

class ConstraintsSpec extends ModelsBaseSpec with Constraints {

  "firstError" should {

    "return Valid when all constraints pass" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""".r, "error.regexp"))("foo")
      result shouldBe Valid
    }

    "return Invalid when the first constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""".r, "error.regexp"))("a" * 11)
      result shouldBe Invalid("error.length", 10)
    }

    "return Invalid when the second constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""".r, "error.regexp"))("")
      result shouldBe Invalid("error.regexp", """^\w+$""")
    }

    "return Invalid for the first error when both constraints fail" in {
      val result = firstError(maxLength(-1, "error.length"), regexp("""^\w+$""".r, "error.regexp"))("")
      result shouldBe Invalid("error.length", -1)
    }
  }

  "minimumValue" should {

    "return Valid for a number greater than the threshold" in {
      val result = minimumValue(1, "error.min").apply(2)
      result shouldBe Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = minimumValue(1, "error.min").apply(1)
      result shouldBe Valid
    }

    "return Invalid for a number below the threshold" in {
      val result = minimumValue(1, "error.min").apply(0)
      result shouldBe Invalid("error.min", 1)
    }
  }

  "maximumValue" should {

    "return Valid for a number less than the threshold" in {
      val result = maximumValue(1, "error.max").apply(0)
      result shouldBe Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = maximumValue(1, "error.max").apply(1)
      result shouldBe Valid
    }

    "return Invalid for a number above the threshold" in {
      val result = maximumValue(1, "error.max").apply(2)
      result shouldBe Invalid("error.max", 1)
    }
  }

  "regexp" should {

    "return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""".r, "error.invalid")("foo")
      result shouldBe Valid
    }

    "return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""".r, "error.invalid")("foo")
      result shouldBe Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" should {

    "return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result shouldBe Valid
    }

    "return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result shouldBe Valid
    }

    "return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result shouldBe Valid
    }

    "return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result shouldBe Invalid("error.length", 10)
    }
  }

  "customNonEmpty" should {
    "return Valid for a string that length is greater than zero" in {
      val result = customNonEmpty("error.key").apply("foo")
      result shouldBe Valid
    }

    "return Invalid for a string that length is zero" in {
      val result = customNonEmpty("custom.error.key").apply("")
      result shouldBe Invalid("custom.error.key")
    }
  }

  "validEmail" should {
    "return Valid if email is empty" in {
      val result = validEmail("custom.error.key").apply("")
      result shouldBe Valid
    }

    "return Valid if email is not empty and email is valid regex" in {
      val result = validEmail("custom.error.key").apply("test@test.com")
      result shouldBe Valid
    }

    "return Invalid if email not empty and email is not valid regex" in {
      val result = validEmail("custom.error.key").apply("a@a")
      result shouldBe Valid
    }

  }

  "maxDate" must {

    def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

      def toMillis(date: LocalDate): Long =
        date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

      Gen.choose(toMillis(min), toMillis(max)).map { millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
      }
    }

    "return Valid for a date before or equal to the maximum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), max)
      } yield (max, date)

      forAll(gen) { case (max, date) =>
        val result = maxDate(max, "error.future")(date)
        result shouldEqual Valid
      }
    }

    "return Invalid for a date after the maximum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(max.plusDays(1), LocalDate.of(3000, 1, 2))
      } yield (max, date)

      forAll(gen) { case (max, date) =>
        val result = maxDate(max, "error.future", "foo")(date)
        result shouldEqual Invalid("error.future", "foo")
      }
    }
  }
}
