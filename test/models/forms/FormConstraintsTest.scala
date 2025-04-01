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

package models.forms

import base.SpecBase
import play.api.data.validation.{Invalid, Valid}

import java.util.Calendar

class FormConstraintsTest extends SpecBase {

  private val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
  // TODO read from appconfig
  private val yearLowBound: Int = 2010

  "FormConstraintsTest" when {

    "dateMustBeInThePast" should {
      "return invalid when date entered is tomorrow" in {
        val date = Calendar.getInstance()
        date.add(Calendar.DAY_OF_MONTH, 1)
        val dateInstant = date.toInstant
        val result      = FormConstraints.dateMustBeInThePast("Test error").apply(dateInstant)

        result shouldBe Invalid("Test error")
      }

      "return invalid when date entered is one month from now" in {
        val date = Calendar.getInstance()
        date.add(Calendar.MONTH, 1)
        val dateInstant = date.toInstant
        val result      = FormConstraints.dateMustBeInThePast("Test error").apply(dateInstant)

        result shouldBe Invalid("Test error")
      }

      "return valid when date entered is in the past (one month ago)" in {
        val date = Calendar.getInstance()
        date.add(Calendar.MONTH, -1)
        val dateInstant = date.toInstant
        val result      = FormConstraints.dateMustBeInThePast("Test error").apply(dateInstant)

        result shouldBe Valid
      }

      "return valid when date entered is in the past (1000 years ago)" in {
        val date = Calendar.getInstance()
        date.add(Calendar.YEAR, -1000)
        val dateInstant = date.toInstant
        val result      = FormConstraints.dateMustBeInThePast("Test error").apply(dateInstant)

        result shouldBe Valid
      }
    }

    "dateLowerBound" should {

      "return valid when year entered is earliest valid" in {
        val date      = Calendar.getInstance()
        val yearsDiff = yearLowBound - currentYear
        date.add(Calendar.YEAR, yearsDiff)

        val yearInstant = date.toInstant
        val result      = FormConstraints.dateLowerBound("Test error", yearLowBound).apply(yearInstant)

        result shouldBe Valid
      }

      "return valid when year entered is in future" in {
        val date      = Calendar.getInstance()
        val yearsDiff = currentYear + 1
        date.add(Calendar.YEAR, yearsDiff)

        val yearInstant = date.toInstant
        val result      = FormConstraints.dateLowerBound("Test error", yearLowBound).apply(yearInstant)

        result shouldBe Valid
      }

      "return invalid when year entered is less than the defined year" in {
        val date      = Calendar.getInstance()
        val yearsDiff = yearLowBound - currentYear - 1

        date.add(Calendar.YEAR, yearsDiff)

        val yearInstant = date.toInstant
        val result      = FormConstraints.dateLowerBound("Test error", yearLowBound).apply(yearInstant)

        result shouldBe Invalid("Test error", yearLowBound.toString)
      }
    }

    "btiReferenceIsCorrectFormat" should {
      "return valid when btiReference entered is in the correct format" in {
        val result = FormConstraints.btiReferenceIsCorrectFormat().apply("1234567")

        result shouldBe Valid
      }

      "return invalid when btiReference entered contains characters" in {
        val result = FormConstraints.btiReferenceIsCorrectFormat().apply("A1234567890")

        result shouldBe Invalid(
          "case.v2.liability.c592.details_edit.bti_reference_error",
          FormConstraints.btiRefRegex.pattern.pattern()
        )
      }

      "return invalid when btiReference entered exceeds the maximum number (22)" in {
        val result = FormConstraints.btiReferenceIsCorrectFormat().apply("123456789123456789012312")

        result shouldBe Invalid(
          "case.v2.liability.c592.details_edit.bti_reference_error",
          FormConstraints.btiRefRegex.pattern.pattern()
        )
      }

      "return invalid when btiReference entered less than minimum number (5)" in {
        val result = FormConstraints.btiReferenceIsCorrectFormat().apply("12345")

        result shouldBe Invalid(
          "case.v2.liability.c592.details_edit.bti_reference_error",
          FormConstraints.btiRefRegex.pattern.pattern()
        )
      }
    }

    "entryNumberIsNumbersAndLettersOnly" should {

      "return valid when entry number does not contain special characters" in {
        val result = FormConstraints.entryNumberIsNumbersAndLettersOnly().apply("123456eadJFG")

        result shouldBe Valid
      }

      "return invalid when entry number contains special characters" in {
        val result = FormConstraints.entryNumberIsNumbersAndLettersOnly().apply("1assed&f23456")

        result shouldBe Invalid(
          "case.liability.error.entry-number",
          FormConstraints.numbersAndLettersRegex.pattern.pattern()
        )
      }
    }

    "dvrNumberIsNumbersAndLettersOnly" should {

      "return valid when dvr number does not contain special characters" in {
        val result = FormConstraints.dvrNumberIsNumberAndLettersOnly().apply("1223345d6")

        result shouldBe Valid
      }

      "return invalid when dvr number contains special characters" in {
        val result = FormConstraints.dvrNumberIsNumberAndLettersOnly().apply("34fadf2&34")

        result shouldBe Invalid(
          "case.liability.error.dvr-number",
          FormConstraints.numbersAndLettersRegex.pattern.pattern()
        )
      }
    }
  }

}
