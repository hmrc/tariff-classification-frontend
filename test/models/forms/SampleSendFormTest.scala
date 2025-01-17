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

import models.{ModelsBaseSpec, SampleSend}

class SampleSendFormTest extends ModelsBaseSpec {

  "Bind from request" should {
    "Bind empty" in {
      val form = SampleSendForm.form.bindFromRequest(Map())

      form.hasErrors            shouldBe true
      form.errors.map(_.message shouldBe "error.empty.sample.sender")
    }

    "Bind blank" in {
      val form = SampleSendForm.form.bindFromRequest(Map("sample-sender" -> Seq("")))

      form.hasErrors shouldBe false
    }

    "Bind valid enum" in {
      val form = SampleSendForm.form.bindFromRequest(Map("sample-sender" -> Seq(SampleSend.TRADER.toString)))

      form.hasErrors shouldBe false
    }

    "Bind invalid enum" in {
      val form = SampleSendForm.form.bindFromRequest(Map("sample-sender" -> Seq("other")))

      form.hasErrors shouldBe true
    }
  }

  "Fill" should {
    "populate empty" in {
      val form = SampleSendForm.form.fill(None)

      form.hasErrors shouldBe false
      form.data      shouldBe Map()
    }

    "populate some" in {
      val form = SampleSendForm.form.fill(Some(SampleSend.AGENT))

      form.hasErrors shouldBe false
      form.data      shouldBe Map("sample-sender" -> "AGENT")
    }
  }

}
