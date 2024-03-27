/*
 * Copyright 2024 HM Revenue & Customs
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

import models.ModelsBaseSpec

class ChangeKeywordStatusFormSpec extends ModelsBaseSpec {

  "Bind from request" should {

    "Bind a blank form" in {
      val form = ChangeKeywordStatusForm.form.bindFromRequest(
        Map(
          "keyword-status" -> Seq()
        )
      )

      form.hasErrors shouldBe false
      form.errors    should have(size(0))
    }

  }
}
