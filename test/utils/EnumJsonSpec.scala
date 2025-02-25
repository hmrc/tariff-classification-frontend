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

package utils

import base.SpecBase
import models.MiscCaseType
import models.MiscCaseType.HARMONISED
import play.api.libs.json._
import utils.JsonFormatters.miscCaseType

class EnumJsonSpec extends SpecBase {

  "EnumJson" when {
    ".enumReads" should {
      "return JsSuccess" when {
        "a JSON with a valid enum name is read" in {
          Json.fromJson[MiscCaseType.Value](JsString("Harmonised systems")) shouldBe JsSuccess(HARMONISED)
        }
      }

      "return JsError" when {
        "a JSON with an invalid enum name is read" in {
          Json.fromJson[MiscCaseType.Value](JsString("enum")) shouldBe JsError(
            "Expected an enumeration of type: 'MiscCaseType$', but it does not contain the name: 'enum'"
          )
        }

        "a JSON with a number value is read" in {
          Json.fromJson[MiscCaseType.Value](JsNumber(1)) shouldBe JsError("String value is expected")
        }
      }
    }

    ".enumWrites" should {
      "return the expected JSON string" in {
        Json.toJson[MiscCaseType.Value](HARMONISED) shouldBe JsString("Harmonised systems")
      }
    }
  }
}
