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

package uk.gov.hmrc.tariffclassificationfrontend.models

import uk.gov.hmrc.play.test.UnitSpec

class SortTest extends UnitSpec {

  "Sort Binder" should {

    "Unbind Populated Sort to Query String" in {
      Sort.bindable.unbind("sort_by", Sort.COMMODITY_CODE) shouldBe "sort_by=commodityCode"
    }

    "Bind empty query string" in {
      Sort.bindable.bind("sort_by", Map()) shouldBe None
    }

    "Bind populated query string" in {
      Sort.bindable.bind("sort_by", Map("sort_by" -> Seq("commodityCode"))) shouldBe Some(Right(Sort.COMMODITY_CODE))
    }

    "Bind invalid query string" in {
      Sort.bindable.bind("sort_by", Map("sort_by" -> Seq("other"))) shouldBe Some(Left("Parameter [sort_by] is invalid"))
    }

  }

}