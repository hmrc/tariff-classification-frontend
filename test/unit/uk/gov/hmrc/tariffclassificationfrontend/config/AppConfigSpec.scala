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

package uk.gov.hmrc.tariffclassificationfrontend.config

import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class AppConfigSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  private val configuration = fakeApplication.configuration
  private val environment = fakeApplication.injector.instanceOf[Environment]
  private val appConfig = new AppConfig(configuration, environment)

  "Config" should {

    "Build assets prefix" in {
      appConfig.assetsPrefix shouldBe "http://localhost:9032/assets/4.5.0"
    }

    "Build analytics token" in {
      appConfig.analyticsToken shouldBe "N/A"
    }

    "Build analytics host" in {
      appConfig.analyticsHost shouldBe "auto"
    }

    "Build report url" in {
      appConfig.reportAProblemPartialUrl shouldBe "http://localhost:9250/contact/problem_reports_ajax?service=MyService"
    }

    "Build report non-json url" in {
      appConfig.reportAProblemNonJSUrl shouldBe "http://localhost:9250/contact/problem_reports_nonjs?service=MyService"
    }

    "Build local Binding Tariff Base URL" in {
      appConfig.bindingTariffClassificationUrl shouldBe "http://localhost:9580"
    }


    "Build auth enrolment" in {
      appConfig.authEnrolment shouldBe "classification"
    }
    
    "Builds whitelist configurations" in {
      appConfig.whitelistDestination shouldBe "https://www.gov.uk"
      appConfig.whitelistedIps shouldBe Seq.empty
      appConfig.whitelistedExcludedPaths shouldBe Seq.empty
    }
  }

}
