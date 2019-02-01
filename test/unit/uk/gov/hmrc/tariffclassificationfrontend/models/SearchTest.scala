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

class SearchTest extends UnitSpec {

  private val populatedSearch = Search(
    traderName = Some("trader-name"),
    commodityCode = Some("commodity-code")
  )

  private val populatedParams: Map[String, Seq[String]] = Map(
    "trader_name" -> Seq("trader-name"),
    "commodity_code" -> Seq("commodity-code")
  )

  private val emptyParams: Map[String, Seq[String]] = Map(
    "trader_name" -> Seq(""),
    "commodity_code" -> Seq("")
  )

  private val populatedQueryParam: String = "trader_name=trader-name&commodity_code=commodity-code"

  /**
  * When we add fields to Search these tests shouldn't need changing, only the fields above.
  **/

  "Search" should {
    "Return isEmpty = true" in {
      val search = Search()
      search.isEmpty shouldBe true
      search.isDefined shouldBe false
    }

    "Return isEmpty = false" in {
      populatedSearch.isEmpty shouldBe false
      populatedSearch.isDefined shouldBe true
    }
  }

  "Search Binder" should {

    "Unbind Unpopulated Search to Query String" in {
      Search.bindable.unbind("", Search()) shouldBe ""
    }

    "Unbind Populated Search to Query String" in {
      Search.bindable.unbind("", populatedSearch) shouldBe populatedQueryParam
    }

    "Bind empty query string" in {
      Search.bindable.bind("", Map()) shouldBe Some(Right(Search()))
    }

    "Bind populated query string" in {
      Search.bindable.bind("", populatedParams) shouldBe Some(Right(populatedSearch))
    }

    "Bind unpopulated query string" in {
      Search.bindable.bind("", emptyParams) shouldBe Some(Right(Search()))
    }

  }

}
