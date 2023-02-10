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

import java.net.URLDecoder

class SearchTest extends ModelsBaseSpec {

  private val populatedSearch = Search(
    caseSource      = Some("trader-name"),
    commodityCode   = Some("commodity-code"),
    decisionDetails = Some("decision-details"),
    status          = Some(Set(PseudoCaseStatus.OPEN, PseudoCaseStatus.LIVE)),
    applicationType = Some(Set(ApplicationType.ATAR, ApplicationType.LIABILITY)),
    keywords        = Some(Set("K1", "K2"))
  )

  /**
    * When we add fields to Search these tests shouldn't need changing, only the field above and vals in each test suite.
    * */
  "Search" should {
    "Return isEmpty = true" in {
      val search = Search()
      search.isEmpty   shouldBe true
      search.isDefined shouldBe false
    }

    "Return isEmpty = true when status is populated" in {
      val search = Search(status = Some(Set(PseudoCaseStatus.OPEN, PseudoCaseStatus.LIVE)))
      search.isEmpty   shouldBe true
      search.isDefined shouldBe false
    }

    "Return isEmpty = false" in {
      populatedSearch.isEmpty   shouldBe false
      populatedSearch.isDefined shouldBe true
    }
  }

  "Search Binder" should {

    val populatedParams: Map[String, Seq[String]] = Map(
      "case_source"         -> Seq("trader-name"),
      "commodity_code"      -> Seq("commodity-code"),
      "decision_details"    -> Seq("decision-details"),
      "application_type[0]" -> Seq("BTI"),
      "application_type[1]" -> Seq("LIABILITY_ORDER"),
      "status[0]"           -> Seq("OPEN"),
      "status[1]"           -> Seq("LIVE"),
      "keyword[0]"          -> Seq("K1"),
      "keyword[1]"          -> Seq("K2")
    )

    val emptyParams: Map[String, Seq[String]] =
      populatedParams.view.mapValues(_ => Seq("")).toMap

    val populatedQueryParam: Set[String] = Set(
      "decision_details=decision-details",
      "case_source=trader-name",
      "commodity_code=commodity-code",
      "application_type[0]=BTI",
      "application_type[1]=LIABILITY_ORDER",
      "status[0]=OPEN",
      "status[1]=LIVE",
      "keyword[0]=K1",
      "keyword[1]=K2"
    )

    "Unbind Unpopulated Search to Query String" in {
      Search.binder.unbind("", Search()) shouldBe ""
    }

    "Unbind Populated Search to Query String" in {
      URLDecoder
        .decode(Search.binder.unbind("", populatedSearch), "UTF-8")
        .split("&")
        .toSet shouldBe populatedQueryParam
    }

    "Bind empty query string" in {
      Search.binder.bind("", Map()) shouldBe Some(Right(Search()))
    }

    "Bind populated query string" in {
      Search.binder.bind("", populatedParams) shouldBe Some(Right(populatedSearch))
    }

    "Bind populated query string with excessive spaces" in {
      val extraSpacesParams =
        populatedParams.view.mapValues(values => values.map(value => s" $value ")).toMap

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
