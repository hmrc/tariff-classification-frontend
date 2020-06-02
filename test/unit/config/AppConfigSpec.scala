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

import base.SpecBase

class AppConfigSpec extends SpecBase {

  "Config" should {

    "Build assets prefix" in {
      realAppConfig.assetsPrefix shouldBe "http://localhost:9032/assets/4.11.0"
    }

    "Build analytics token" in {
      realAppConfig.analyticsToken shouldBe "N/A"
    }

    "Build analytics host" in {
      realAppConfig.analyticsHost shouldBe "auto"
    }

    "Build report url" in {
      realAppConfig.reportAProblemPartialUrl shouldBe "http://localhost:9250/contact/problem_reports_ajax?service=tariff-classification-frontend"
    }

    "Build report non-json url" in {
      realAppConfig.reportAProblemNonJSUrl shouldBe "http://localhost:9250/contact/problem_reports_nonjs?service=tariff-classification-frontend"
    }

    "Build local Binding Tariff Base URL" in {
      realAppConfig.bindingTariffClassificationUrl shouldBe "http://localhost:9580"
    }

    "Build local Ruling Base URL" in {
      realAppConfig.rulingUrl shouldBe "http://localhost:9586"
    }

    "Build local Email URL" in {
      realAppConfig.emailUrl shouldBe "http://localhost:8300"
    }

    "Build local Email Renderer URL" in {
      realAppConfig.emailRendererUrl shouldBe "http://localhost:8950"
    }

    "Build team enrolment" in {
      realAppConfig.teamEnrolment shouldBe "classification"
    }

    "Build manager enrolment" in {
      realAppConfig.managerEnrolment shouldBe "classification-manager"
    }

    "Build enrolment flag" in {
      realAppConfig.checkEnrolment shouldBe true
    }

    "Builds decisionLifetimeYears" in {
      realAppConfig.decisionLifetimeYears shouldBe 3
    }

    "Build Clock" in {
      realAppConfig.clock shouldBe Clock.systemUTC()
    }

    "Build API Token" in {
      realAppConfig.apiToken shouldBe "9253947-99f3-47d7-9af2-b75b4f37fd34"
    }

    "Build Commodity Code Path" in {
      realAppConfig.commodityCodePath shouldBe "commodityCodes-local.csv"
    }

    "build 'filestore' url" in {
      realAppConfig.fileStoreUrl shouldBe "http://localhost:9583"
    }

    "shutter urls excluded" in {

      realAppConfig.shutterExcludedUrls shouldBe "/ping/ping"
    }

    "build new-liability-details" in {
      realAppConfig.newLiabilityDetails shouldBe true
    }
  }

}
