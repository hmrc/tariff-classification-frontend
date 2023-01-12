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

import models.{ModelsBaseSpec, SampleStatus}

class SampleStatusFormTest extends ModelsBaseSpec {

  "Bind from request" should {
    "Bind empty" in {
      val form = SampleStatusForm.form.bindFromRequest(Map())

      form.hasErrors shouldBe true
    }

    "Bind blank" in {
      val form = SampleStatusForm.form.bindFromRequest(Map("status" -> Seq("")))

      form.hasErrors shouldBe false
    }

    "Bind valid enum" in {
      val form = SampleStatusForm.form.bindFromRequest(Map("status" -> Seq(SampleStatus.AWAITING.toString)))

      form.hasErrors shouldBe false
    }

    "Bind invalid enum" in {
      val form = SampleStatusForm.form.bindFromRequest(Map("status" -> Seq("other")))

      form.hasErrors shouldBe true
    }
  }

  "Fill" should {
    "populate empty" in {
      val form = SampleStatusForm.form.fill(None)

      form.hasErrors shouldBe false
      form.data      shouldBe Map()
    }

    "populate some" in {
      val form = SampleStatusForm.form.fill(Some(SampleStatus.AWAITING))

      form.hasErrors shouldBe false
      form.data      shouldBe Map("status" -> "AWAITING")
    }
  }

}
