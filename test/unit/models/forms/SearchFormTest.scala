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

import play.api.data.FormError
import uk.gov.hmrc.play.test.UnitSpec
import models.{ApplicationType, PseudoCaseStatus, Search}

class SearchFormTest extends UnitSpec {

  private val commodityCodeError = "Commodity code must be empty or numeric between 2 and 22 digits"

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
          "decision_details" -> Seq(""),
          "status[0]" -> Seq(""),
          "application_type[0]" -> Seq(""),
          "keyword[0]" -> Seq("")
        )
      ).errors shouldBe Seq.empty
    }

    "disallow short commodity code" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq("0")
        )
      ).errors shouldBe Seq(FormError("commodity_code", List(commodityCodeError)))
    }

    "disallow long commodity code" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq("0" * 23)
        )
      ).errors shouldBe Seq(FormError("commodity_code", List(commodityCodeError)))
    }

    "disallow non-numerical commodity code" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq("eee")
        )
      ).errors shouldBe Seq(FormError("commodity_code", List(commodityCodeError)))
    }

    "maps to data" in {
      SearchForm.form.bindFromRequest(
        Map(
          "commodity_code" -> Seq("00"),
          "trader_name" -> Seq("trader-name"),
          "decision_details" -> Seq("decision-details"),
          "status[0]" -> Seq("OPEN"),
          "status[1]" -> Seq("LIVE"),
          "application_type[0]" -> Seq("BTI"),
          "application_type[1]" -> Seq("LIABILITY_ORDER"),
          "keyword[0]" -> Seq("X"),
          "keyword[1]" -> Seq("Y")
        )
      ).get shouldBe Search(
        traderName = Some("trader-name"),
        commodityCode = Some("00"),
        decisionDetails = Some("decision-details"),
        status = Some(Set(PseudoCaseStatus.OPEN, PseudoCaseStatus.LIVE)),
        applicationType = Some(Set(ApplicationType.BTI, ApplicationType.LIABILITY_ORDER)),
        keywords = Some(Set("X", "Y"))
      )
    }

    "maps from data" in {
      SearchForm.form.fill(Search(
        traderName = Some("trader-name"),
        commodityCode = Some("00"),
        decisionDetails = Some("decision-details"),
        status = Some(Set(PseudoCaseStatus.OPEN, PseudoCaseStatus.LIVE)),
        applicationType = Some(Set(ApplicationType.BTI, ApplicationType.LIABILITY_ORDER)),
        keywords = Some(Set("X", "Y"))
      )).data shouldBe Map[String, String](
        "trader_name" -> "trader-name",
        "commodity_code" -> "00",
        "decision_details" -> "decision-details",
        "status[0]" -> "OPEN",
        "status[1]" -> "LIVE",
        "application_type[0]" -> "BTI",
        "application_type[1]" -> "LIABILITY_ORDER",
        "keyword[0]" -> "X",
        "keyword[1]" -> "Y"
      )
    }
  }
}
