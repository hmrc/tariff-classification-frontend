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

package uk.gov.hmrc.tariffclassificationfrontend.filters

import akka.stream.Materializer
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.http.HttpVerbs.GET
import play.api.mvc.Call
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig

class WhitelistFilterSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val mockMaterializer = mock[Materializer]
  private val appConfig = mock[AppConfig]

  private val whitelistFilter = new WhitelistFilter(appConfig, mockMaterializer)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(appConfig)
  }

  "WhitelistFilter" should {

    "throw an error if an expected configuration is missing" in {
      when(appConfig.whitelistedIps).thenThrow(new RuntimeException)
      when(appConfig.whitelistDestination).thenThrow(new RuntimeException)
      when(appConfig.whitelistedExcludedPaths).thenThrow(new RuntimeException)

      assertThrows[RuntimeException] {
        whitelistFilter.whitelist
      }
      assertThrows[RuntimeException] {
        whitelistFilter.destination
      }
      assertThrows[RuntimeException] {
        whitelistFilter.excludedPaths
      }
    }

    "behave as expected" in {
      when(appConfig.whitelistedIps).thenReturn(Seq("a.b.c.d", "z.x.y.w"))
      when(appConfig.whitelistDestination).thenReturn("www.google.com")
      when(appConfig.whitelistedExcludedPaths).thenReturn(Seq("/", "/hello"))

      whitelistFilter.whitelist shouldBe Seq("a.b.c.d", "z.x.y.w")
      whitelistFilter.destination shouldBe Call(GET, "www.google.com")
      whitelistFilter.excludedPaths shouldBe Seq(Call(GET, "/"), Call(GET, "/hello"))
    }

  }

}
