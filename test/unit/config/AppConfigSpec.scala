/*
 * Copyright 2020 HM Revenue & Customs
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

package config

import java.time.Clock

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.play.test.UnitSpec

class AppConfigSpec extends UnitSpec with MockitoSugar with GuiceOneAppPerSuite {

  override lazy val app: Application = new GuiceApplicationBuilder().overrides().build()

  lazy val appConf = app.injector.instanceOf[AppConfig]


  "Config" should {

    "Build assets prefix" in {
      appConf.assetsPrefix shouldBe "http://localhost:9032/assets/4.11.0"
    }

    "Build analytics token" in {
      appConf.analyticsToken shouldBe "N/A"
    }

    "Build analytics host" in {
      appConf.analyticsHost shouldBe "auto"
    }

    "Build report url" in {
      appConf.reportAProblemPartialUrl shouldBe "http://localhost:9250/contact/problem_reports_ajax?service=tariff-classification-frontend"
    }

    "Build report non-json url" in {
      appConf.reportAProblemNonJSUrl shouldBe "http://localhost:9250/contact/problem_reports_nonjs?service=tariff-classification-frontend"
    }

    "Build local Binding Tariff Base URL" in {
      appConf.bindingTariffClassificationUrl shouldBe "http://localhost:9580"
    }

    "Build local Ruling Base URL" in {
      appConf.rulingUrl shouldBe "http://localhost:9586"
    }

    "Build local Email URL" in {
      appConf.emailUrl shouldBe "http://localhost:8300"
    }

    "Build local Email Renderer URL" in {
      appConf.emailRendererUrl shouldBe "http://localhost:8950"
    }

    "Build team enrolment" in {
      appConf.teamEnrolment shouldBe "classification"
    }

    "Build manager enrolment" in {
      appConf.managerEnrolment shouldBe "classification-manager"
    }

    "Build enrolment flag" in {
      appConf.checkEnrolment shouldBe true
    }

    "Builds decisionLifetimeYears" in {
      appConf.decisionLifetimeYears shouldBe 3
    }

    "Build Clock" in {
      appConf.clock shouldBe Clock.systemUTC()
    }

    "Build API Token" in {
      appConf.apiToken shouldBe "9253947-99f3-47d7-9af2-b75b4f37fd34"
    }

    "Build Commodity Code Path" in {
      appConf.commodityCodePath shouldBe "commodityCodes-local.csv"
    }

    "build 'filestore' url" in {
      appConf.fileStoreUrl shouldBe "http://localhost:9583"
    }

    "shutter urls excluded" in {

      appConf.shutterExcludedUrls shouldBe "/ping/ping"
    }

    "build new-liability-details" in {
      appConf.newLiabilityDetails shouldBe false
    }
  }

}
