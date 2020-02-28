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

import uk.gov.hmrc.play.test.UnitSpec

class MandatoryBooleanFormTest extends UnitSpec {

  "Boolean Form 'Bind From Request'" should {
    "fail on empty request" in {
      BooleanForm.form.bindFromRequest(Map("state" -> Seq())).hasErrors shouldBe true
      BooleanForm.form.bindFromRequest(Map("state" -> Seq(""))).hasErrors shouldBe true
    }

    "fail on non boolean request" in {
      MandatoryBooleanForm.form().bindFromRequest(Map("state" -> Seq("123"))).hasErrors shouldBe true
    }

    "fail on empty" in {
      val emptyForm = MandatoryBooleanForm.form().bindFromRequest(Map())
      emptyForm.hasErrors shouldBe true
    }

    "succeed on valid request" in {
      val falsyForm = MandatoryBooleanForm.form().bindFromRequest(Map("state" -> Seq("false")))
      falsyForm.hasErrors shouldBe false
      falsyForm.get shouldBe false

      val truthyForm = MandatoryBooleanForm.form().bindFromRequest(Map("state" -> Seq("true")))
      truthyForm.hasErrors shouldBe false
      truthyForm.get shouldBe true
    }
  }

  "Boolean Form 'fill'" should {
    "pre populate form" in {
      MandatoryBooleanForm.form().fill(true).data shouldBe Map("state" -> "true")
      MandatoryBooleanForm.form().fill(false).data shouldBe Map("state" -> "false")
    }
  }

}
