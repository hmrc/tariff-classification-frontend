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

class CancelReasonTest extends ModelsBaseSpec {

  "Review format" should {
    "format 'Annulled'" in {
      CancelReason.format(CancelReason.ANNULLED) shouldBe "Annulled (55)"
    }

    "format 'Invalidated Code Change'" in {
      CancelReason.format(
        CancelReason.INVALIDATED_CODE_CHANGE
      ) shouldBe "Invalidated due to nomenclature code changes (61)"
    }

    "format 'Invalidated EU Measure'" in {
      CancelReason.format(CancelReason.INVALIDATED_EU_MEASURE) shouldBe "Invalidated due to EU measure (62)"
    }

    "format 'Invalidated National Measure'" in {
      CancelReason.format(
        CancelReason.INVALIDATED_NATIONAL_MEASURE
      ) shouldBe "Invalidated due to national legal measure (63)"
    }

    "format 'Invalidated Wrong Classification'" in {
      CancelReason.format(
        CancelReason.INVALIDATED_WRONG_CLASSIFICATION
      ) shouldBe "Invalidated due to incorrect classification (64)"
    }

    "format 'Invalidated Other'" in {
      CancelReason.format(CancelReason.INVALIDATED_OTHER) shouldBe "Invalidated due to other reasons (65)"
    }

    "format 'Other'" in {
      CancelReason.format(CancelReason.OTHER) shouldBe "Invalidated due to other reasons"
    }
  }

  "Review code" should {
    "format 'Annulled'" in {
      CancelReason.code(CancelReason.ANNULLED) shouldBe Some(55)
    }

    "format 'Invalidated Code Change'" in {
      CancelReason.code(CancelReason.INVALIDATED_CODE_CHANGE) shouldBe Some(61)
    }

    "format 'Invalidated EU Measure'" in {
      CancelReason.code(CancelReason.INVALIDATED_EU_MEASURE) shouldBe Some(62)
    }

    "format 'Invalidated National Measure'" in {
      CancelReason.code(CancelReason.INVALIDATED_NATIONAL_MEASURE) shouldBe Some(63)
    }

    "format 'Invalidated Wrong Classification'" in {
      CancelReason.code(CancelReason.INVALIDATED_WRONG_CLASSIFICATION) shouldBe Some(64)
    }

    "format 'Invalidated Other'" in {
      CancelReason.code(CancelReason.INVALIDATED_OTHER) shouldBe Some(65)
    }

    "format 'Other'" in {
      CancelReason.code(CancelReason.OTHER) shouldBe None
    }
  }

}
