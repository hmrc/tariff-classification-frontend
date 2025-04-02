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

package models

import base.SpecBase
import play.api.libs.json.{JsError, Json}

class CountrySpec extends SpecBase {
  "Country" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Country(
          code = "sds",
          countryName = "sdf",
          alphaTwoCode = "sdfs",
          countrySynonyms = List("sdd", "aeds")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Country]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[Country] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("min" -> "ad", "max" -> 23)
        json.validate[Country] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Country] shouldBe a[JsError]
      }
    }
  }
}
