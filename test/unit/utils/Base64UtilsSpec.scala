/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

class Base64UtilsSpec extends UtilsBaseSpec {

  private val text: String    = "hello world"
  private val encoded: String = "aGVsbG8gd29ybGQ="

  "Base64Utils" should {
    "Convert string to Base64" in {
      Base64Utils.encode(text) shouldBe encoded
    }

    "Convert Base64 to string" in {
      Base64Utils.decode(encoded) shouldBe text
    }

    "Encode, reverting itself" in {
      Base64Utils.decode(Base64Utils.encode(text)) shouldBe text
    }

    "Decode, reverting itself" in {
      Base64Utils.encode(Base64Utils.decode(encoded)) shouldBe encoded
    }
  }

}
