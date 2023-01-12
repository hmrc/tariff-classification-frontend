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

class CorrespondenceDetailsFormSpec extends ModelsBaseSpec {

  private val correspondenceCase = Cases.correspondenceCaseExample
  private val sampleEmptyCase    = Cases.correspondenceCaseExample.copy(application = Cases.corrExampleWithMissingFields)

  private val caseWithoutSummary =
    correspondenceCase.copy(application = Cases.corrExampleWithMissingFields.copy(summary = ""))

  private val params = Map(
    "summary"             -> Seq("A short summary"),
    "detailedDescription" -> Seq("A detailed desc")
  )

  "CorrespondenceDetailsForm" should {
    "Fail to bind" when {
      "a case with mandatorys are missing" in {
        CorrespondenceDetailsForm
          .correspondenceDetailsForm(sampleEmptyCase)
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
      CorrespondenceDetailsForm
        .correspondenceDetailsForm(correspondenceCase)
        .fold(
          _ => "form should not have errors",
          aCase => aCase shouldBe correspondenceCase
        )

    }

    "fail to bind with correct error messages" when {

      "summary is empty" in {
        CorrespondenceDetailsForm
          .correspondenceDetailsForm(caseWithoutSummary)
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
    }

    "Fill" should {
      "populate by default" in {
        val form = CorrespondenceDetailsForm.correspondenceDetailsForm(correspondenceCase)

        form.hasErrors shouldBe false
        form.data      shouldBe params.mapValues(v => v.head)
      }
    }
  }
}
