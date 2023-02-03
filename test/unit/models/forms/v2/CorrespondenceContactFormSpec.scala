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

class CorrespondenceContactFormSpec extends ModelsBaseSpec {

  private val correspondenceCase = Cases.correspondenceCaseExample
  private val sampleEmptyCase    = Cases.correspondenceCaseExample.copy(application = Cases.corrExampleWithMissingFields)

  private val emptyCaseWithEmail = sampleEmptyCase.copy(
    application = Cases.corrExampleWithMissingFields.copy(contact = Contact("name", "valid@email", Some("123")))
  )

  private val emptyCaseWithInvalidEmail = correspondenceCase.copy(
    application = Cases.corrExampleWithMissingFields
      .copy(correspondenceStarter = Some("case-source"), contact = Contact("name", "email", Some("123")))
  )

  private val emptyCaseWithInvalidPostcode = correspondenceCase.copy(
    application = Cases.corrExampleWithMissingFields
      .copy(
        correspondenceStarter = Some("case-source"),
        contact               = Contact("name", "valid@email", Some("123")),
        address               = Address("buildingAndStreet", "townOrCity", Some("county"), Some("1234567890"))
      )
  )

  private val params = Map(
    "correspondenceStarter" -> Seq("Starter"),
    "name"                  -> Seq("a name"),
    "email"                 -> Seq("anemail@some.com"),
    "buildingAndStreet"     -> Seq("New building"),
    "townOrCity"            -> Seq("Old Town"),
    "agentName"             -> Seq("Agent 007")
  )

  "CorrespondenceContactForm" should {
    "Fail to bind" when {
      "a case with mandatorys are missing" in {
        CorrespondenceContactForm
          .correspondenceContactForm(sampleEmptyCase)
          .fold(
            form => {
              form.hasErrors         shouldBe true
              form.errors.size       shouldBe 2
              form.errors.map(_.key) shouldBe Seq("correspondenceStarter", "email")
            },
            _ => "form should not succeed"
          )
      }
    }

    "Bind valid form" in {
      CorrespondenceContactForm
        .correspondenceContactForm(correspondenceCase)
        .fold(
          _ => "form should not have errors",
          aCase => aCase shouldBe correspondenceCase
        )

    }

    "fail to bind with correct error messages" when {

      "correspondenceStarter is empty" in {
        CorrespondenceContactForm
          .correspondenceContactForm(emptyCaseWithEmail)
          .fold(
            form => {
              form.hasErrors         shouldBe true
              form.errors.size       shouldBe 1
              form.errors.map(_.key) shouldBe Seq("correspondenceStarter")
              form.error("correspondenceStarter").map(_.message shouldBe "Enter a case source")

            },
            _ => "form should not succeed"
          )

      }

      "email is not valid" in {
        CorrespondenceContactForm
          .correspondenceContactForm(emptyCaseWithInvalidEmail)
          .fold(
            form => {
              form.hasErrors         shouldBe true
              form.errors.size       shouldBe 1
              form.errors.map(_.key) shouldBe Seq("email")
              form.error("email").map(_.message shouldBe "case.liability.error.trader.email")
            },
            _ => "form should not succeed"
          )

      }

      "postcode is not valid" in {
        CorrespondenceContactForm
          .correspondenceContactForm(emptyCaseWithInvalidPostcode)
          .fold(
            form => {
              form.hasErrors         shouldBe true
              form.errors.size       shouldBe 2
              form.errors.map(_.key) shouldBe Seq("postCode", "postCode")
              form.errors.flatMap(_.messages) shouldBe Seq(
                "case.liability.error.postcode.valid",
                "case.liability.error.postcode.length"
              )
            },
            _ => "form should not succeed"
          )
      }
    }

    "Fill" should {
      "populate by default" in {
        val form = CorrespondenceContactForm.correspondenceContactForm(correspondenceCase)

        form.hasErrors shouldBe false
        form.data      shouldBe params.view.mapValues(v => v.head).toMap
      }
    }
  }
}
