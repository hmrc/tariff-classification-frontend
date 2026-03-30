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

package models.forms.mappings

import models.CancelReason
import base.SpecBase
import play.api.data.FormError

class FormMappingsTest extends SpecBase {

  "One of" should {

    "return valid val if part of enum" in {
      val mapping = FormMappings.oneOf("My Error", CancelReason)
      val result  = mapping.bind(Map("" -> "INVALIDATED_CODE_CHANGE"))

      result shouldBe Right("INVALIDATED_CODE_CHANGE")
    }

    "return form error if value is invalid" in {
      val mapping = FormMappings.oneOf("My Error", CancelReason)
      val result  = mapping.bind(Map("" -> "CABBAGE"))

      result shouldBe Left(Seq(FormError("", "My Error")))
    }

    "return form error if value is missing" in {
      val mapping = FormMappings.oneOf("My Error", CancelReason)
      val result  = mapping.bind(Map())

      result shouldBe Left(Seq(FormError("", "My Error")))
    }

    "return form error if value is set to nothing" in {
      val mapping = FormMappings.oneOf("My Error", CancelReason)
      val result  = mapping.bind(Map("" -> ""))

      result shouldBe Left(Seq(FormError("", "My Error")))
    }
  }

  "field non empty" should {
    "return valid val if some text present" in {
      val mapping = FormMappings.fieldNonEmpty("My Error")
      val result  = mapping.bind(Map("" -> "Some value"))

      result shouldBe Right("Some value")
    }

    "return form error if value missing" in {
      val mapping = FormMappings.fieldNonEmpty("My Error")
      val result  = mapping.bind(Map())

      result shouldBe Left(Seq(FormError("", "My Error")))
    }
  }

  "text non empty" should {
    "return valid val if some text present" in {
      val mapping = FormMappings.textNonEmpty("My Error")
      val result  = mapping.bind(Map("" -> "Some value"))

      result shouldBe Right("Some value")
    }

    "return form error if value present but empty" in {
      val mapping = FormMappings.textNonEmpty("My Error")
      val result  = mapping.bind(Map("" -> ""))

      result shouldBe Left(Seq(FormError("", "My Error")))
    }

    "return form error if value missing" in {
      val mapping = FormMappings.textNonEmpty("My Error")
      val result  = mapping.bind(Map())

      result shouldBe Left(Seq(FormError("", "My Error")))
    }
  }
}
