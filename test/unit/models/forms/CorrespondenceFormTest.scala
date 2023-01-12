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

package models.forms

import models._

class CorrespondenceFormTest extends ModelsBaseSpec {

  "Bind from request" should {
    "Bind an empty form" in {
      val form = CorrespondenceForm.newCorrespondenceForm.bindFromRequest(Map())

      form.hasErrors shouldBe true
      form.errors    should have(size(3))
    }

    "Bind a blank form" in {
      val form = CorrespondenceForm.newCorrespondenceForm.bindFromRequest(
        Map(
          "summary"      -> Seq(""),
          "source"       -> Seq(""),
          "contactEmail" -> Seq("")
        )
      )

      form.hasErrors shouldBe true
      form.errors    should have(size(2))
    }

    "Bind a valid form" in {
      val form = CorrespondenceForm.newCorrespondenceForm.bindFromRequest(
        Map(
          "summary"      -> Seq("example"),
          "source"       -> Seq("example"),
          "contactEmail" -> Seq("example@email.com")
        )
      )

      form.hasErrors shouldBe false
      form.get shouldBe CorrespondenceApplication(
        correspondenceStarter = Some("example"),
        agentName             = None,
        contact               = Contact("", "example@email.com", None),
        summary               = "example",
        detailedDescription   = "",
        sampleToBeProvided    = false,
        sampleToBeReturned    = false,
        address               = Address("", "", None, None)
      )
    }

    "Bind invalid email" in {
      val form = CorrespondenceForm.newCorrespondenceForm.bindFromRequest(
        Map(
          "summary"      -> Seq("example"),
          "source"       -> Seq("example"),
          "contactEmail" -> Seq("exampleemail.com")
        )
      )

      form.hasErrors shouldBe true
      form.errors    should have(size(1))
      //TODO get message for messages
      form.errors.head.message shouldBe "case.liability.error.email"
    }

    "Bind empty source" in {
      val form = CorrespondenceForm.newCorrespondenceForm.bindFromRequest(
        Map(
          "summary"      -> Seq("example"),
          "source"       -> Seq(""),
          "contactEmail" -> Seq("example@email.com")
        )
      )

      form.hasErrors shouldBe true
      form.errors    should have(size(1))
      //TODO get message for messages
      form.errors.head.message shouldBe "Please enter a case source"
    }

    "Bind empty summary" in {
      val form = CorrespondenceForm.newCorrespondenceForm.bindFromRequest(
        Map(
          "summary"      -> Seq(""),
          "source"       -> Seq("example"),
          "contactEmail" -> Seq("example@email.com")
        )
      )

      form.hasErrors shouldBe true
      form.errors    should have(size(1))
      //TODO get message for messages
      form.errors.head.message shouldBe "Please enter a short description"
    }
  }

  "Fill" should {

    "populate a correct form" in {
      val form = CorrespondenceForm.newCorrespondenceForm.fill(
        CorrespondenceApplication(
          correspondenceStarter = Some("source"),
          agentName             = None,
          contact               = Contact("", "contact@email.com", None),
          summary               = "shortDescr",
          detailedDescription   = "",
          sampleToBeProvided    = false,
          sampleToBeReturned    = false,
          address               = Address("", "", None, None)
        )
      )

      form.hasErrors shouldBe false
      form.data shouldBe Map(
        "summary"      -> "shortDescr",
        "source"       -> "source",
        "contactEmail" -> "contact@email.com"
      )
    }
  }

}
