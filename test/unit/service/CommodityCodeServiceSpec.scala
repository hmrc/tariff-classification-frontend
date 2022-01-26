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

package service

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}

import config.AppConfig
import models.CommodityCode
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterEach

class CommodityCodeServiceSpec extends ServiceSpecBase with BeforeAndAfterEach {

  private val config = mock[AppConfig]

  private def service = new CommodityCodeService(config)

  override def beforeEach(): Unit = {
    super.beforeEach()
    given(config.clock) willReturn Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
  }

  "Commodity code service" should {

    "not find non-leaf codes" in {
      given(config.commodityCodePath) willReturn "commodityCodes-local.csv"
      service.find("0400000000").isDefined shouldBe false
    }

    "find codes 10 characters in length" in {
      given(config.commodityCodePath) willReturn "commodityCodes-local.csv"
      service.find("0300000000").isDefined   shouldBe true
      service.find(" 0300000000 ").isDefined shouldBe true
    }

    "find codes less than 10 characters in length" in {
      given(config.commodityCodePath) willReturn "commodityCodes-local.csv"
      service.find("0300").isDefined   shouldBe true
      service.find(" 0300 ").isDefined shouldBe true
    }

    "find codes longer than 10 characters" in {
      given(config.commodityCodePath) willReturn "commodityCodes-local.csv"
      service.find("0300000000123456789").isDefined   shouldBe true
      service.find(" 0300000000123456789 ").isDefined shouldBe true
    }

    "not find codes from single digit chapters that are missing the leading zero" when {
      "using production dataset" in {
        given(config.commodityCodePath) willReturn "commodityCodes.csv"
        service.find("409").isDefined   shouldBe false
        service.find(" 409 ").isDefined shouldBe false
      }
    }

    "not find codes enter in pairs-of-digits format" when {
      "using production dataset" in {
        given(config.commodityCodePath) willReturn "commodityCodes.csv"
        service.find("04 09 00 00 00").isDefined  shouldBe false
        service.find("04 09 00 00 00 ").isDefined shouldBe false
      }
    }

    "not find codes that are missing form the file" in {
      given(config.commodityCodePath) willReturn "commodityCodes-local.csv"
      service.find("9999999999").isDefined  shouldBe false
      service.find("9999999999 ").isDefined shouldBe false
    }

    "find commodity codes with optional end dates" in {
      given(config.commodityCodePath) willReturn "commodityCodes-local.csv"
      service.find("0100000000") shouldBe Some(CommodityCode("0100000000", Some("2019-01-01T00:00:00")))
      service.find("0200000000") shouldBe Some(CommodityCode("0200000000", Some("3000-01-01T00:00:00")))
      service.find("0300000000") shouldBe Some(CommodityCode("0300000000", None))
    }

    implicit def str2instant: String => Instant = LocalDateTime.parse(_).toInstant(ZoneOffset.UTC)

  }

}
