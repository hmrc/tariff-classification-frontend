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

import scala.util.{Failure, Try}

class WhitelistFilterSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val mat = mock[Materializer]
  private val appConfig = mock[AppConfig]

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(appConfig)
  }

  "WhitelistFilter" should {

    "behave as expected when the whitelisting configurations are set" in {
      when(appConfig.whitelistedIps).thenReturn(Seq("a.b.c.d", "z.x.y.w"))
      when(appConfig.whitelistDestination).thenReturn("www.google.com")
      when(appConfig.whitelistedExcludedPaths).thenReturn(Seq("/", "/hello"))

      val whitelistFilter = new WhitelistFilter(appConfig, mat)

      whitelistFilter.whitelist shouldBe Seq("a.b.c.d", "z.x.y.w")
      whitelistFilter.destination shouldBe Call(GET, "www.google.com")
      whitelistFilter.excludedPaths shouldBe Seq(Call(GET, "/"), Call(GET, "/hello"))
    }

    "behave as expected when the whitelisting configurations are missing" in {
      val error = new RuntimeException("simulated error")

      var errorCount = 0

      def tryExec(block: => Unit): Unit = {
        Try(block) match {
          case Failure(e) if e == error => errorCount = 1 + errorCount
          case x => throw new IllegalStateException(s"Unexpected: $x")
        }
      }

      when(appConfig.whitelistedIps).thenThrow(error)
      when(appConfig.whitelistDestination).thenThrow(error)
      when(appConfig.whitelistedExcludedPaths).thenThrow(error)

      val whitelistFilter = new WhitelistFilter(appConfig, mat)

      tryExec(whitelistFilter.whitelist)
      tryExec(whitelistFilter.destination)
      tryExec(whitelistFilter.excludedPaths)

      errorCount shouldBe 3
    }

  }

}
