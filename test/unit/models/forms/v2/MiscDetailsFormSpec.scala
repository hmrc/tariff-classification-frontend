/*
 * Copyright 2023 HM Revenue & Customs
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

package models.forms.v2

import models._
import utils.Cases

class MiscDetailsFormSpec extends ModelsBaseSpec {

  private val miscCase        = Cases.miscellaneousCaseExample
  private val sampleEmptyCase = Cases.miscellaneousCaseExample.copy(application = Cases.miscExampleWithMissingName)

  private val params = Map(
    "detailedDescription" -> Seq("A detailed description"),
    "contactName"         -> Seq(""),
    "caseType"            -> Seq("Harmonised systems"),
    "boardsFileNumber"    -> Seq("SOC/554/2015/JN"),
    "summary"             -> Seq("name")
  )

  "MiscDetailsForm" should {
    "Fail to bind" when {
      "a case with mandatorys field is missing" in {
        MiscDetailsForm
          .miscDetailsForm(sampleEmptyCase)
          .fold(
            form => {
              form.hasErrors         shouldBe true
              form.errors.size       shouldBe 1
              form.errors.map(_.key) shouldBe Seq("summary")
            },
            _ => "form should not succeed"
          )
      }
    }

    "Bind valid form" in {
      MiscDetailsForm
        .miscDetailsForm(miscCase)
        .fold(
          _ => "form should not have errors",
          aCase => aCase shouldBe miscCase
        )

    }

    "fail to bind with correct error messages" when {

      "summary is empty" in {
        MiscDetailsForm
          .miscDetailsForm(sampleEmptyCase)
          .fold(
            form => {
              form.hasErrors         shouldBe true
              form.errors.size       shouldBe 1
              form.errors.map(_.key) shouldBe Seq("summary")
              form.error("summary").map(_.message shouldBe "Enter a summary")

            },
            _ => "form should not succeed"
          )

      }

      "case type is not recognised" in {
        MiscDetailsForm
          .miscDetailsForm(miscCase)
          .copy(data = Map("caseType" -> "unrecognised"))
          .fold(
            form => {
              form.hasErrors         shouldBe true
              form.errors.size       shouldBe 1
              form.errors.map(_.key) shouldBe Seq("caseType")
              form.error("summary").map(_.message shouldBe "error.empty.miscCaseType")

            },
            _ => "form should not succeed"
          )
      }
    }

    "Fill" should {
      "populate by default" in {
        val form = MiscDetailsForm.miscDetailsForm(miscCase)

        form.hasErrors shouldBe false
        form.data      shouldBe params.map { case (fst, snd) => fst -> snd.head }
      }
    }
  }
}
