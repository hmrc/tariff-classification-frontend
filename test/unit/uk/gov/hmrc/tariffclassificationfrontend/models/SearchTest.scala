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

import java.net.URLDecoder

import uk.gov.hmrc.play.test.UnitSpec

class SearchTest extends UnitSpec {

  private val populatedSearch = Search(
    traderName = Some("trader-name"),
    commodityCode = Some("commodity-code"),
    liveRulingsOnly = Some(true),
    keywords = Some(Set("K1", "K2"))
  )

  /**
  * When we add fields to Search these tests shouldn't need changing, only the field above and vals in each test suite.
  **/

  "Search" should {
    "Return isEmpty = true" in {
      val search = Search()
      search.isEmpty shouldBe true
      search.isDefined shouldBe false
    }

    "Return isEmpty = true when liveRulingsOnly is populated" in {
      val search = Search(liveRulingsOnly = Some(true))
      search.isEmpty shouldBe true
      search.isDefined shouldBe false
    }

    "Return isEmpty = false" in {
      populatedSearch.isEmpty shouldBe false
      populatedSearch.isDefined shouldBe true
    }
  }

  "Search Binder" should {

    val populatedParams: Map[String, Seq[String]] = Map(
      "trader_name" -> Seq("trader-name"),
      "commodity_code" -> Seq("commodity-code"),
      "live_rulings_only" -> Seq("true"),
      "keyword[0]" -> Seq("K1"),
      "keyword[1]" -> Seq("K2")
    )

    val emptyParams: Map[String, Seq[String]] = Map(
      "trader_name" -> Seq(""),
      "commodity_code" -> Seq(""),
      "live_rulings_only" -> Seq(""),
      "keyword[0]" -> Seq(""),
      "keyword[1]" -> Seq("")
    )

    val populatedQueryParam: String = "keyword[0]=K1&trader_name=trader-name&keyword[1]=K2&commodity_code=commodity-code&live_rulings_only=true"

    "Unbind Unpopulated Search to Query String" in {
      Search.binder.unbind("", Search()) shouldBe ""
    }

    "Unbind Populated Search to Query String" in {
      URLDecoder.decode(Search.binder.unbind("", populatedSearch), "UTF-8") shouldBe populatedQueryParam
    }

    "Bind empty query string" in {
      Search.binder.bind("", Map()) shouldBe Some(Right(Search()))
    }

    "Bind populated query string" in {
      Search.binder.bind("", populatedParams) shouldBe Some(Right(populatedSearch))
    }

    "Bind populated query string with excessive spaces" in {
      val extraSpacesParams = populatedParams.mapValues(values => values.map(value => s" $value "))
      Search.binder.bind("", extraSpacesParams) shouldBe Some(Right(populatedSearch))
    }

    "Bind unpopulated query string" in {
      Search.binder.bind("", emptyParams) shouldBe Some(Right(Search()))
    }

    "Bind query string with errors" in {
      Search.binder.bind("", Map("live_rulings_only" -> Seq("abc"))) shouldBe Some(Right(Search()))
    }

  }

}
