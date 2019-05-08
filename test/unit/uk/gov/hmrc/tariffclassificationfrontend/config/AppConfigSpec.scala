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

package uk.gov.hmrc.tariffclassificationfrontend.config

import java.time.Clock

import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.test.UnitSpec

class AppConfigSpec extends UnitSpec with MockitoSugar {

  def appConfig(pairs: (String, String)*): AppConfig = {
    var defaultConfig: Map[String, String] = Map()
    pairs.foreach(e => defaultConfig = defaultConfig + e)
    new AppConfig(Configuration.from(defaultConfig), Environment.simple())
  }

  "Config" should {

    "Build assets prefix" in {
      appConfig(
        "assets.url" -> "http://localhost:9032/assets/",
        "assets.version" -> "4.5.0"
      ).assetsPrefix shouldBe "http://localhost:9032/assets/4.5.0"
    }

    "Build analytics token" in {
      appConfig("google-analytics.token" -> "N/A").analyticsToken shouldBe "N/A"
    }

    "Build analytics host" in {
      appConfig("google-analytics.host" -> "auto").analyticsHost shouldBe "auto"
    }

    "Build report url" in {
      appConfig("contact-frontend.host" -> "host").reportAProblemPartialUrl shouldBe "host/contact/problem_reports_ajax?service=MyService"
    }

    "Build report non-json url" in {
      appConfig("contact-frontend.host" -> "host").reportAProblemNonJSUrl shouldBe "host/contact/problem_reports_nonjs?service=MyService"
    }

    "Build local Binding Tariff Base URL" in {
      appConfig(
        "microservice.services.binding-tariff-classification.host" -> "host",
        "microservice.services.binding-tariff-classification.port" -> "123"
      ).bindingTariffClassificationUrl shouldBe "http://host:123"
    }

    "Build local Ruling Base URL" in {
      appConfig(
        "microservice.services.binding-tariff-ruling-frontend.host" -> "host",
        "microservice.services.binding-tariff-ruling-frontend.port" -> "123"
      ).rulingUrl shouldBe "http://host:123"
    }

    "Build local Email URL" in {
      appConfig(
        "microservice.services.email.host" -> "host",
        "microservice.services.email.port" -> "123"
      ).emailUrl shouldBe "http://host:123"
    }

    "Build local Email Renderer URL" in {
      appConfig(
        "microservice.services.hmrc-email-renderer.host" -> "host",
        "microservice.services.hmrc-email-renderer.port" -> "123"
      ).emailRendererUrl shouldBe "http://host:123"
    }

    "Build team enrolment" in {
      appConfig("auth.enrolments.team" -> "classification").teamEnrolment shouldBe "classification"
    }

    "Build manager enrolment" in {
      appConfig("auth.enrolments.manager" -> "classification").managerEnrolment shouldBe "classification"
    }

    "Build enrolment flag" in {
      appConfig("auth.enrolments.enabled" -> "true").checkEnrolment shouldBe true
    }

    "Builds runningAsDev from config override" in {
      appConfig("run.mode" -> "Dev").runningAsDev shouldBe true
      appConfig("run.mode" -> "Prod").runningAsDev shouldBe false
    }

    "Builds runningAsDev from mode" in {
      val config = Configuration.from(Map())
      new AppConfig(config, Environment.simple(mode = Mode.Dev)).runningAsDev shouldBe true
      new AppConfig(config, Environment.simple(mode = Mode.Test)).runningAsDev shouldBe false
    }

    "Builds runningAsDev giving precedence to config override" in {
      val testConfig = Configuration.from(Map("run.mode" -> "Test"))
      new AppConfig(testConfig, Environment.simple(mode = Mode.Dev)).runningAsDev shouldBe false

      val devConfig = Configuration.from(Map("run.mode" -> "Dev"))
      new AppConfig(devConfig, Environment.simple(mode = Mode.Test)).runningAsDev shouldBe true
    }

    "Builds decisionLifetimeYears" in {
      appConfig("app.decision-lifetime-years" -> "1").decisionLifetimeYears shouldBe 1
    }

    "Build Clock" in {
      appConfig().clock shouldBe Clock.systemUTC()
    }

    "Build API Token" in {
      appConfig("auth.api-token" -> "token").apiToken shouldBe "token"
    }

    "Build Commodity Code Path" in {
      appConfig("app.commodity-code-path" -> "path.csv").commodityCodePath shouldBe "path.csv"
    }

    "build 'filestore' url" in {
      appConfig(
        "microservice.services.binding-tariff-filestore.protocol" -> "https",
        "microservice.services.binding-tariff-filestore.host" -> "www.host.co.uk",
        "microservice.services.binding-tariff-filestore.port" -> "123"
      ).fileStoreUrl shouldBe "https://www.host.co.uk:123"
    }
  }

}
