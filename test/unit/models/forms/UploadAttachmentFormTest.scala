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
import models.AppealStatus

class UploadAttachmentFormTest extends UnitSpec {

  "Bind from request" should {
    "Bind empty" in {
      val form = UploadAttachmentForm.form.bindFromRequest(Map())

      form.hasErrors shouldBe true
    }


    "Bind data" in {
      val form = UploadAttachmentForm.form.bindFromRequest(Map("file-input" -> Seq("other.pdf")))

      form.hasErrors shouldBe false
    }
  }

  "Fill" should {
    "populate some" in {
      val form = UploadAttachmentForm.form.fill("data.pdf")

      form.hasErrors shouldBe false
      form.data shouldBe Map("file-input" -> "data.pdf")
    }
  }



}
