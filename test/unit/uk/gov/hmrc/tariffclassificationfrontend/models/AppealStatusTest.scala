/*
 * Copyright 2019 HM Revenue & Customs
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

class AppealStatusTest extends UnitSpec {

  "Appeal format" should {

    "format 'In Progress'" in {
      AppealStatus.format(Some(AppealStatus.IN_PROGRESS)) shouldBe "Under appeal"
    }

    "format 'allowed'" in {
      AppealStatus.format(Some(AppealStatus.ALLOWED)) shouldBe "Appeal allowed"
    }

    "format 'dismissed'" in {
      AppealStatus.format(Some(AppealStatus.DISMISSED)) shouldBe "Appeal dismissed"
    }

    "format 'none'" in {
      AppealStatus.format(None) shouldBe "None"
    }

  }

}
