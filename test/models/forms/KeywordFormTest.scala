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

import base.SpecBase
import play.api.data.FormError

class KeywordFormTest extends SpecBase {

  "Keywords form " should {
    "validate 'keyword'" in {
      KeywordForm.form
        .bindFromRequest(
          Map(
            "keyword" -> Seq("")
          )
        )
        .errors shouldBe Seq(FormError("keyword", "error.empty.keyword"))
    }

    "accept a keyword" in {
      KeywordForm.form
        .bindFromRequest(
          Map(
            "keyword" -> Seq("FOOD")
          )
        )
        .value shouldBe Some("FOOD")
    }

    "don't allow missing fields" in {
      KeywordForm.form
        .bindFromRequest(
          Map()
        )
        .errors shouldBe Seq(FormError("keyword", "error.empty.keyword"))
    }

    "fill in form correctly" in {
      val keyWordForm = KeywordForm
      keyWordForm.form.fill("FOOD").data shouldBe Map("keyword" -> "FOOD")
    }
  }

}
