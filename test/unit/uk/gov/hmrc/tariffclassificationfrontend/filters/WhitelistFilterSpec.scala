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

package uk.gov.hmrc.tariffclassificationfrontend

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import org.scalatest.TestData
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.HttpVerbs.GET
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.{Application, Configuration, Environment}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.filters.WhitelistFilter

import scala.util.{Failure, Try}

class IPWhitelistFilterSpec extends UnitSpec with MockitoSugar with GuiceOneAppPerTest {

  private val mat = mock[Materializer]

  private def createFilter: WhitelistFilter = {
    val environment = app.injector.instanceOf[Environment]
    val appConfig = new AppConfig(app.configuration, environment)
    new WhitelistFilter(appConfig, mat)
  }

  override def newAppForTest(testData: TestData): Application = {

    testData.name match {

      case testName if testName.matches("^.*set$") =>
        val configuration = Map(
          "whitelist.allowedIps" -> " a.b.c.d,  z.x.y.w ",
          "whitelist.excluded" -> " /,   /hello/",
          "whitelist.destination" -> "http://localhost/"
        )
        GuiceApplicationBuilder(
          configuration = Configuration.from(configuration)
        ).build()

      case testName if testName.matches("^.*missing$") =>
        GuiceApplicationBuilder().loadConfig(
          new Configuration(ConfigFactory.load("empty-application.conf"))
        ).build()

      case testName => throw new IllegalArgumentException(s"Test scenario not expected: $testName ")

    }

  }

  "WhitelistFilter" should {

    "behave as expected when all whitelisting configurations are set" in {
      val whitelistFilter = createFilter

      whitelistFilter.whitelist shouldBe Seq("a.b.c.d", "z.x.y.w")
      whitelistFilter.destination shouldBe Call(GET, "http://localhost/")
      whitelistFilter.excludedPaths shouldBe Seq(Call(GET, "/"), Call(GET, "/hello/"))
    }

    "behave as expected when all whitelisting configurations are missing" in {
      var errorCount = 0

      def tryExec(block: => Unit): Unit = {
        Try(block) match {
          case Failure(_: RuntimeException) => errorCount = 1 + errorCount
          case x => throw new IllegalArgumentException(s"Unexpected: $x")
        }
      }

      val whitelistFilter = createFilter

      tryExec(whitelistFilter.whitelist)
      tryExec(whitelistFilter.destination)
      tryExec(whitelistFilter.excludedPaths)

      errorCount shouldBe 3
    }

  }

}
