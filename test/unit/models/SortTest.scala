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

package models

class SortTest extends ModelsBaseSpec {

  "SortField Binder" should {

    "Unbind Populated SortField to Query String" in {
      SortField.bindable.unbind("sort_by", SortField.COMMODITY_CODE) shouldBe "sort_by=commodity-code"
    }

    "Bind empty query string" in {
      SortField.bindable.bind("sort_by", Map()) shouldBe None
    }

    "Bind populated query string" in {
      SortField.bindable.bind("sort_by", Map("sort_by" -> Seq("commodity-code"))) shouldBe Some(
        Right(SortField.COMMODITY_CODE)
      )
    }

    "Bind invalid query string" in {
      SortField.bindable.bind("sort_by", Map("sort_by" -> Seq("other"))) shouldBe Some(
        Left("Parameter [sort_by] is invalid")
      )
    }

  }

  "SortDirection Binder" should {

    "Unbind Populated SortDirection to Query String" in {
      SortDirection.bindable.unbind("sort_direction", SortDirection.ASCENDING)  shouldBe "sort_direction=asc"
      SortDirection.bindable.unbind("sort_direction", SortDirection.DESCENDING) shouldBe "sort_direction=desc"
    }

    "Bind empty query string" in {
      SortDirection.bindable.bind("sort_direction", Map()) shouldBe None
    }

    "Bind populated query string" in {
      SortDirection.bindable.bind("sort_direction", Map("sort_direction" -> Seq("asc"))) shouldBe Some(
        Right(SortDirection.ASCENDING)
      )
      SortDirection.bindable.bind("sort_direction", Map("sort_direction" -> Seq("desc"))) shouldBe Some(
        Right(SortDirection.DESCENDING)
      )
    }

    "Bind invalid query string" in {
      SortDirection.bindable.bind("sort_direction", Map("sort_direction" -> Seq("other"))) shouldBe Some(
        Left("Parameter [sort_direction] is invalid")
      )
    }

  }

  "Sort Binder" should {
    val sort = Sort(SortDirection.ASCENDING, SortField.COMMODITY_CODE)

    "Unbind Populated SortDirection to Query String" in {
      Sort.bindable.unbind("", sort) shouldBe "sort_direction=asc&sort_by=commodity-code"
    }

    "Bind empty query string" in {
      Sort.bindable.bind("", Map()) shouldBe Some(Right(Sort()))
    }

    "Bind populated query string with a sort_direction and sort_by" in {
      Sort.bindable.bind("", Map("sort_direction" -> Seq("asc"), "sort_by" -> Seq("commodity-code"))) shouldBe Some(
        Right(sort)
      )
    }

    "Bind populated query string with only a sort_by" in {
      Sort.bindable.bind("", Map("sort_by" -> Seq("commodity-code"))) shouldBe Some(
        Right(Sort(field = SortField.COMMODITY_CODE))
      )
    }

    "Bind populated query string with only a sort_direction" in {
      Sort.bindable.bind("", Map("sort_direction" -> Seq("asc"))) shouldBe Some(
        Right(Sort(direction = SortDirection.ASCENDING))
      )
    }

    "Bind invalid query string" in {
      Sort.bindable.bind("", Map("sort_direction" -> Seq("other"), "sort_by" -> Seq("other"))) shouldBe Some(
        Right(Sort())
      )
    }

  }
}
