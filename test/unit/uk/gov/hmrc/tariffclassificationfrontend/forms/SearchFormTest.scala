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

    "allow missing fields" in {
      SearchForm.form.bindFromRequest(
        Map()
      ).errors shouldBe Seq.empty
    }

    "allow empty fields" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq(""),
          "trader_name" -> Seq(""),
          "good_description" -> Seq(""),
          "live_rulings_only" -> Seq(""),
          "keyword" -> Seq("")
        )
      ).errors shouldBe Seq.empty
    }

    "disallow short commodity code" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq("0")
        )
      ).errors shouldBe Seq(FormError("commodity_code", List("Must be at least 2 characters")))
    }

    "disallow long commodity code" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq("0" * 23)
        )
      ).errors shouldBe Seq(FormError("commodity_code", List("Must be 22 characters or less")))
    }

    "disallow non-numerical commodity code" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq("eee")
        )
      ).errors shouldBe Seq(FormError("commodity_code", List("Must be numerical")))
    }

    "maps to data" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq("00"),
          "trader_name" -> Seq("trader-name"),
          "good_description" -> Seq("good-description"),
          "live_rulings_only" -> Seq("true"),
          "keyword[0]" -> Seq("X"),
          "keyword[1]" -> Seq("Y")
        )
      ).get shouldBe Search(
        traderName = Some("trader-name"),
        commodityCode = Some("00"),
        goodDescription = Some("good-description"),
        liveRulingsOnly = Some(true),
        keywords = Some(Set("X", "Y"))
      )
    }

    "maps from data" in {
      SearchForm.form.fill(Search(
        traderName = Some("trader-name"),
        commodityCode = Some("00"),
        goodDescription = Some("good-description"),
        liveRulingsOnly = Some(true),
        keywords = Some(Set("X", "Y"))
      )).data shouldBe Map(
        "trader_name" -> "trader-name",
        "commodity_code" -> "00",
        "good_description" -> "good-description",
        "live_rulings_only" -> "true",
        "keyword[0]" -> "X",
        "keyword[1]" -> "Y"
      )
    }
  }
}
