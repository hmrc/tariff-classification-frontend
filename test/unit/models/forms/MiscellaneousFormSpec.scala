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

import models._
import models.forms.v2.MiscellaneousForm

class MiscellaneousFormSpec extends ModelsBaseSpec {

  "Bind from request" should {
    "Bind an empty form" in {
      val form = MiscellaneousForm.newMiscForm.bindFromRequest(Map())

      form.hasErrors shouldBe true
      form.errors    should have(size(3))
    }

    "Bind a blank form" in {
      val form = MiscellaneousForm.newMiscForm.bindFromRequest(
        Map(
          "detailedDescription" -> Seq(""),
          "contactName"      -> Seq(""),
          "caseType"        -> Seq("")
        )
      )

      form.hasErrors shouldBe true
      form.errors    should have(size(3))
    }

    "Bind a valid form" in {
      val form =  MiscellaneousForm.newMiscForm.bindFromRequest(
        Map(
          "detailedDescription" -> Seq("example"),
          "contactName"      -> Seq("example"),
          "caseType"        -> Seq("Other government dept")
        )
      )

      form.hasErrors shouldBe false
      form.get shouldBe MiscApplication(
        contact = Contact("", "example", None),
        offline = false,
        name = "",
        contactName = Some("example"),
        caseType = MiscCaseType.withName("Other government dept"),
        detailedDescription = Some("example"),
        sampleToBeProvided = false,
        sampleToBeReturned = false,
        messagesLogged = List.empty

      )
    }



    "Bind empty description" in {
      val form = MiscellaneousForm.newMiscForm.bindFromRequest(
        Map(
          "detailedDescription" -> Seq(""),
          "contactName"      -> Seq("example"),
          "caseType"        -> Seq("Other government dept")
        )
      )

      form.hasErrors shouldBe true
      form.errors    should have(size(1))
      form.errors.head.message shouldBe "error.empty.misc.shortDesc"
    }

    "Bind empty contact name" in {
      val form = MiscellaneousForm.newMiscForm.bindFromRequest(
        Map(
          "detailedDescription" -> Seq("example"),
          "contactName"      -> Seq(""),
          "caseType"        -> Seq("Other government dept")
        )
      )

      form.hasErrors shouldBe true
      form.errors    should have(size(1))
      form.errors.head.message shouldBe "error.empty.misc.contactName"
    }
  }

  "Fill" should {

    "populate a correct form" in {
      val form =MiscellaneousForm.newMiscForm.fill(
        MiscApplication(
          contact = Contact("", "example", None),
          offline = false,
          name = "",
          contactName = Some("example"),
          caseType = MiscCaseType.withName("Other government dept"),
          detailedDescription = Some("example"),
          sampleToBeProvided = false,
          sampleToBeReturned = false,
          messagesLogged = List.empty
        )
      )

      form.hasErrors shouldBe false
      form.data shouldBe Map(
        "detailedDescription" -> "example",
        "contactName"      -> "example",
        "caseType"        -> "Other government dept"
      )
    }
  }

}
