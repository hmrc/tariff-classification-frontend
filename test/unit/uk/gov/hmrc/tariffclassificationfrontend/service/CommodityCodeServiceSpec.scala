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

package uk.gov.hmrc.tariffclassificationfrontend.service

import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class CommodityCodeServiceSpec extends UnitSpec with MockitoSugar {

  private val service = new CommodityCodeService()

  "Commodity code service" should {

    "find codes 10 characters in length" in {
      service.checkIfCodeExists("0409000000") shouldBe true
      service.checkIfCodeExists("0409000000 ") shouldBe true
    }

    "find codes less than 10 characters in length" in {
      service.checkIfCodeExists("0409") shouldBe true
      service.checkIfCodeExists("0409 ") shouldBe true
    }

    "find codes longer than 10 characters" in {
      service.checkIfCodeExists("0409000000123456789") shouldBe true
      service.checkIfCodeExists("0409000000123456789 ") shouldBe true
    }

    "not find codes that are missing form the file" in {
      service.checkIfCodeExists("9999999999") shouldBe false
      service.checkIfCodeExists("9999999999 ") shouldBe false
    }

    "not find codes from single digit chapters that are missing the leading zero" in {
      service.checkIfCodeExists("409") shouldBe false
      service.checkIfCodeExists("409 ") shouldBe false
    }

    "not find codes enter in pairs-of-digits format" in {
      service.checkIfCodeExists("04 09 00 00 00") shouldBe false
      service.checkIfCodeExists("04 09 00 00 00 ") shouldBe false
    }
  }

}
