/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.models

import uk.gov.hmrc.play.test.UnitSpec
import util.oCase

class ApplicationSpec extends UnitSpec {

  "Application 'Get Type'" should {

    "convert liability order type" in {
      oCase.liabilityApplicationExample.getType shouldBe "Liability Order"
    }

    "convert bti type" in {
      oCase.btiApplicationExample.getType shouldBe "BTI"
    }

  }

  "Application 'Is BTI'" should {

    "be truthy for a BTI" in {
      oCase.btiApplicationExample.isBTI shouldBe true
    }

    "be falsy for another type" in {
      oCase.liabilityApplicationExample.isBTI shouldBe false
    }

  }

  "Application 'Is Liability Order'" should {

    "be truthy for a Liability" in {
      oCase.liabilityApplicationExample.isLiabilityOrder shouldBe true
    }

    "be falsy for another type" in {
      oCase.btiApplicationExample.isLiabilityOrder shouldBe false
    }

  }

  "Application 'As BTI'" should {

    "cast a BTI" in {
      oCase.btiApplicationExample.asBTI shouldBe a[BTIApplication]
    }

    "fail to case another type" in {
      assertThrows[RuntimeException] {
        oCase.liabilityApplicationExample.asBTI
      }
    }

  }

  "Application 'As Liability'" should {

    "cast a Liability" in {
      oCase.liabilityApplicationExample.asLiabilityOrder shouldBe a[LiabilityOrder]
    }

    "fail to case another type" in {
      assertThrows[RuntimeException] {
        oCase.btiApplicationExample.asLiabilityOrder
      }
    }

  }

}
