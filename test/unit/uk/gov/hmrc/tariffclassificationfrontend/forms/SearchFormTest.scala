/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.Search

class SearchFormTest extends UnitSpec {

  "Search Form" should {
    "fill" in {
      SearchForm.fill(Search(
        traderName = Some("trader")
      )).data shouldBe Map(
        "trader_name" -> "trader"
      )
    }

    "validate 'Trader Name'" in {
      SearchForm.form.bindFromRequest(
        Map(
          "trader_name" -> Seq("")
        )
      ).errors shouldBe Seq(FormError("trader_name", "error.required"))
    }
  }
}
