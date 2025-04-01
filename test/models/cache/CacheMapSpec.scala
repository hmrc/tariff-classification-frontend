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

package models.cache

import base.SpecBase
import play.api.libs.json.{JsBoolean, JsError, JsString, JsValue, Json}

class CacheMapSpec extends SpecBase {
  "CacheMap" should {
    "should throw KeyStoreEntryValidationException when value is not found" when {
      "getEntry is called" in {
        val cacheMap = new CacheMap(
          id = "sdd",
          data = Map(("test", JsString("sad")), ("data", JsBoolean(true)))
        )
        val caught = intercept[KeyStoreEntryValidationException] {
          cacheMap.getEntry[Int]("test")
        }
        caught.getMessage shouldBe "KeyStore entry for key 'test' was '\"sad\"'. Attempt to convert to models.cache.CacheMap$ gave errors: List((,List(JsonValidationError(List(error.expected.jsnumber),ArraySeq()))))"
      }
    }
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new CacheMap(
          id = "sdd",
          data = Map(("asd", JsString("sad")))
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CacheMap]

        deserialized shouldBe original
      }
      "map is empty" in {
        val original = new CacheMap(
          id = "sdd",
          data = Map.empty
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CacheMap]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[CacheMap] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("min" -> "ad", "max" -> 23)
        json.validate[CacheMap] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CacheMap] shouldBe a[JsError]
      }
    }
  }
}
