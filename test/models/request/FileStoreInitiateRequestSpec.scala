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

package models.request

import base.SpecBase
import play.api.libs.json.{JsError, Json}

class FileStoreInitiateRequestSpec extends SpecBase {
  "FileStoreInitiateRequest" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = FileStoreInitiateRequest(
          id = Some("123as"),
          successRedirect = Some("dccds"),
          errorRedirect = Some("sdcvsd"),
          expectedContentType = Some("cdwcs"),
          publishable = false,
          maxFileSize = 4
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[FileStoreInitiateRequest]

        deserialized shouldBe original
      }
      "all of them are default values" in {
        val original     = new FileStoreInitiateRequest(maxFileSize = 1)
        val json         = Json.toJson(original)
        val deserialized = json.as[FileStoreInitiateRequest]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[FileStoreInitiateRequest] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("maxFileSize" -> "ad", "id" -> 23)
        json.validate[FileStoreInitiateRequest] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("maxFileSize" -> "ad", "id" -> 23)
        json.validate[FileStoreInitiateRequest] shouldBe a[JsError]
      }
    }
    "deserialize when maxFileSize is an int" in {
      val json = Json.obj("maxFileSize" -> 1)
      json.as[FileStoreInitiateRequest] shouldBe FileStoreInitiateRequest(maxFileSize = 1)
    }
  }
}
