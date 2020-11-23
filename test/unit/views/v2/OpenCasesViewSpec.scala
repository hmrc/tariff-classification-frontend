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

package views.v2

import models.viewmodels.{ATaRTab, CasesTabViewModel}
import views.ViewSpec
import views.html.v2.common_all_open_cases_view
import views.ViewMatchers.containElementWithID

class OpenCasesViewSpec extends ViewSpec {


  def commonCasesView: common_all_open_cases_view = injector.instanceOf[common_all_open_cases_view]

  "OpenCasesViewSpec" should {

    "render successfully with the default tab" in {
      val doc = view(commonCasesView("title", CasesTabViewModel.atar))

      doc should containElementWithID("open-cases-tabs")
    }
  }

  "contain an atar, cap, cars and elm tabs for ATaR" in {
    val doc = view(commonCasesView("title", CasesTabViewModel.atar))

    doc should containElementWithID("act_tab")
    doc should containElementWithID("cars_tab")
    doc should containElementWithID("elm_tab")
    doc should containElementWithID("flex_tab")
    doc should containElementWithID("tta_tab")
    doc should containElementWithID("ttb_tab")
    doc should containElementWithID("ttc_tab")
  }

  "contain a heading" in {
    val doc = view(commonCasesView("title", CasesTabViewModel.atar))

    doc should containElementWithID("common-cases-heading")
  }

  "contain open-cases-sub-nav" in {
    val doc = view(commonCasesView("title", CasesTabViewModel.atar))

    doc should containElementWithID("open-cases-sub-nav")
  }

}
