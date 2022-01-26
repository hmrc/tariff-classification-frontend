/*
 * Copyright 2022 HM Revenue & Customs
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

import models.ModelsBaseSpec

class BooleanFormTest extends ModelsBaseSpec {

  "Boolean Form 'Bind From Request'" should {
    "fail on empty request" in {
      BooleanForm.form.bindFromRequest(Map("state" -> Seq())).hasErrors   shouldBe true
      BooleanForm.form.bindFromRequest(Map("state" -> Seq(""))).hasErrors shouldBe true
    }

    "fail on non boolean request" in {
      BooleanForm.form.bindFromRequest(Map("state" -> Seq("123"))).hasErrors shouldBe true
    }

    "succeed on valid request" in {
      val emptyForm = BooleanForm.form.bindFromRequest(Map())
      emptyForm.hasErrors shouldBe false
      emptyForm.get       shouldBe false

      val falsyForm = BooleanForm.form.bindFromRequest(Map("state" -> Seq("false")))
      falsyForm.hasErrors shouldBe false
      falsyForm.get       shouldBe false

      val truthyForm = BooleanForm.form.bindFromRequest(Map("state" -> Seq("true")))
      truthyForm.hasErrors shouldBe false
      truthyForm.get       shouldBe true
    }
  }

  "Boolean Form 'fill'" should {
    "pre populate form" in {
      BooleanForm.form.fill(true).data  shouldBe Map("state" -> "true")
      BooleanForm.form.fill(false).data shouldBe Map("state" -> "false")
    }
  }

}
