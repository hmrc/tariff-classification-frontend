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

package models.response

import base.SpecBase
import play.api.libs.json.{JsError, Json}

class FileStoreInitiateResponseSpec extends SpecBase {
  "FileStoreInitiateResponse" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = FileStoreInitiateResponse(
          id = "dsfcda",
          upscanReference = "sdfc",
          uploadRequest = UpscanFormTemplate(
            "http://localhost:20001/upscan/upload",
            Map("key" -> "value")
          )
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[FileStoreInitiateResponse]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[FileStoreInitiateResponse] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("upscanReference" -> "ad", "id" -> 23)
        json.validate[FileStoreInitiateResponse] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("id" -> "23")
        json.validate[FileStoreInitiateResponse] shouldBe a[JsError]
      }
    }
  }
}
