/*
 * Copyright 2021 HM Revenue & Customs
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

import utils.Cases

class ApplicationSpec extends ModelsBaseSpec {

  "Application 'Get Type'" should {

    "convert liability order type" in {
      Cases.liabilityApplicationExample.getType shouldBe "Liability"
    }

    "convert bti type" in {
      Cases.btiApplicationExample.getType shouldBe "BTI"
    }

    "convert correspondence type" in {
      Cases.correspondenceExample.getType shouldBe "Correspondence"
    }

    "convert misc type" in {
      Cases.miscExample.getType shouldBe "Misc"
    }

  }

  "Application 'Is BTI'" should {

    "be truthy for a BTI" in {
      Cases.btiApplicationExample.isBTI shouldBe true
    }

    "be falsy for another type" in {
      Cases.liabilityApplicationExample.isBTI shouldBe false
    }

  }

  "Application 'Is Liability Order'" should {

    "be truthy for a Liability" in {
      Cases.liabilityApplicationExample.isLiabilityOrder shouldBe true
    }

    "be falsy for another type" in {
      Cases.btiApplicationExample.isLiabilityOrder shouldBe false
    }

  }

  "Application 'Is Live Liability Order'" should {

    "be truthy for a Live Liability" in {
      Cases.liabilityApplicationExample.copy(status = LiabilityStatus.LIVE).isLiveLiabilityOrder shouldBe true
    }

    "be falsy for another type" in {
      Cases.liabilityApplicationExample.copy(status = LiabilityStatus.NON_LIVE).isLiveLiabilityOrder shouldBe false
      Cases.btiApplicationExample.isLiveLiabilityOrder shouldBe false
    }

  }

  "Application 'Is Correspondence'" should {

    "be truthy for a Correspondence" in {
      Cases.correspondenceExample.isCorrespondence shouldBe true
    }

    "be falsy for another type" in {
      Cases.btiApplicationExample.isCorrespondence shouldBe false
    }

  }

  "Application 'Is Misc'" should {

    "be truthy for a Misc" in {
      Cases.miscExample.isMisc shouldBe true
    }

    "be falsy for another type" in {
      Cases.btiApplicationExample.isMisc shouldBe false
    }

  }

  "Application 'As BTI'" should {

    "cast a BTI" in {
      Cases.btiApplicationExample.asATAR shouldBe a[BTIApplication]
    }

    "fail to case another type" in {
      assertThrows[RuntimeException] {
        Cases.liabilityApplicationExample.asATAR
      }
    }

  }

  "Application 'As Liability'" should {

    "cast a Liability" in {
      Cases.liabilityApplicationExample.asLiabilityOrder shouldBe a[LiabilityOrder]
    }

    "fail to case another type" in {
      assertThrows[RuntimeException] {
        Cases.btiApplicationExample.asLiabilityOrder
      }
    }

    "Application 'As Correspondence'" should {

      "cast a Correspondence" in {
        Cases.correspondenceExample.asCorrespondence shouldBe a[CorrespondenceApplication]
      }

      "fail to case another type" in {
        assertThrows[RuntimeException] {
          Cases.btiApplicationExample.asCorrespondence
        }
      }
    }

    "Application 'As Misc'" should {

      "cast a Misc" in {
        Cases.miscExample.asMisc shouldBe a[MiscApplication]
      }

      "fail to case another type" in {
        assertThrows[RuntimeException] {
          Cases.btiApplicationExample.asMisc
        }
      }
    }

    "Application goodsName" should {

      "return a value for BTI application" in {
        Cases.btiApplicationExample.goodsName shouldBe "Laptop"
      }

      "return a value for liability order" in {
        Cases.liabilityApplicationExample.goodsName shouldBe "good-name"
      }
    }

  }

  "Application 'Business Name'" should {

    "return for Liability" in {
      Cases.liabilityApplicationExample.businessName shouldBe Some(Cases.liabilityApplicationExample.asLiabilityOrder.traderName)
    }

    "return for BTI" in {
      Cases.btiApplicationExample.businessName shouldBe Some(Cases.btiApplicationExample.asATAR.holder.businessName)
    }

  }

}
