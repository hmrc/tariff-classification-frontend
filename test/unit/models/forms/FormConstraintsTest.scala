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

import java.util.Calendar

import play.api.data.validation.{Invalid, Valid}
import uk.gov.hmrc.play.test.UnitSpec

class FormConstraintsTest extends UnitSpec {

  "FormConstraintsTest" when {

    "dateMustBeInThePast" should {
      "return invalid when date entered is tomorrow" in {
        val date = Calendar.getInstance()
        date.add(Calendar.DAY_OF_MONTH, 1)
        val dateInstant = date.toInstant
        val result = FormConstraints.dateMustBeInThePast("date.must.be.in.past").apply(dateInstant)

        result shouldBe Invalid("date.must.be.in.past")
      }

      "return invalid when date entered is one month from now" in {
        val date = Calendar.getInstance()
        date.add(Calendar.MONTH, 1)
        val dateInstant = date.toInstant
        val result = FormConstraints.dateMustBeInThePast("date.must.be.in.past").apply(dateInstant)

        result shouldBe Invalid("date.must.be.in.past")
      }

      "return valid when date entered is in the past (one month ago)" in {
        val date = Calendar.getInstance()
        date.add(Calendar.MONTH, -1)
        val dateInstant = date.toInstant
        val result = FormConstraints.dateMustBeInThePast("date.must.be.in.past").apply(dateInstant)

        result shouldBe Valid
      }
    }

    //TODO Pass the correct error message
    "dateLowerBound" should {

      "return valid when year entered is not before the defined year" in {
        val definedLowerBoundsYear = 2010
        val date = Calendar.getInstance()
        date.add(Calendar.YEAR, -10)

        val yearInstant = date.toInstant
        val result = FormConstraints.dateLowerBound("Entry date error error error", definedLowerBoundsYear).apply(yearInstant)

        result shouldBe Valid
      }

      "return invalid when year entered is greater than the defined year" in {
        val definedLowerBoundsYear = 2010
        val date = Calendar.getInstance()
        date.add(Calendar.YEAR, -20)

        val yearInstant = date.toInstant
        val result = FormConstraints.dateLowerBound("Entry date error error error", definedLowerBoundsYear).apply(yearInstant)

        result shouldBe Invalid("Entry date error error error")
      }
    }

    "btiReferenceIsCorrectFormat" should {
      "return valid when btiReference entered is in the correct format" in {
        val result = FormConstraints.btiReferenceIsCorrectFormat.apply("1234567")

        result shouldBe Valid
      }

      "return invalid when btiReference entered contains characters" in {
        val result = FormConstraints.btiReferenceIsCorrectFormat.apply("ASDFASASF")

        result shouldBe Invalid("case.v2.liability.c592.details_edit.bti_reference_error", """[0-9]{6,22}""")
      }

      "return invalid when btiReference entered exceeds the maximum number (22)" in {
        val result = FormConstraints.btiReferenceIsCorrectFormat.apply("123456789123456789012312")

        result shouldBe Invalid("case.v2.liability.c592.details_edit.bti_reference_error", """[0-9]{6,22}""")
      }
    }

    "entryNumberIsNumberOnly" should {

      "return valid when entry number does not contain characters" in {
        val result = FormConstraints.entryNumberIsNumberOnly.apply("123456")

        result shouldBe Valid
      }

      "return invalid when entry number contains characters" in {
        val result = FormConstraints.entryNumberIsNumberOnly.apply("1assedf23456")

        result shouldBe Invalid("case.liability.error.entry-number", """^\d+$""")
      }
    }

    "dvrNumberIsNumberOnly" should {

      "return valid when dvr number does not contain characters" in {
        val result = FormConstraints.dvrNumberIsNumberOnly.apply("12233456")

        result shouldBe Valid
      }

      "return invalid when dvr number contains characters" in {
        val result = FormConstraints.dvrNumberIsNumberOnly.apply("34fadf234")

        result shouldBe Invalid("case.liability.error.dvr-number", """^\d+$""")
      }
    }
  }

}
