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

import views.ViewSpec
import views.html.v2.common_all_open_cases_view
import views.ViewMatchers.containElementWithID

class OpenCasesViewSpec extends ViewSpec {


  def commonCasesView: common_all_open_cases_view = injector.instanceOf[common_all_open_cases_view]

  "OpenCasesViewSpec" should {

    "render successfully" in {
      val doc = view(commonCasesView("title"))

      doc should containElementWithID("open-cases-tabs")
    }
  }

  "contain an atar, cap, cars and elm tabs" in {
    val doc = view(commonCasesView("title"))

    doc should containElementWithID("act_tab")
    doc should containElementWithID("cap_tab")
    doc should containElementWithID("cars_tab")
    doc should containElementWithID("elm_tab")
  }

  "contain a heading" in {
    val doc = view(commonCasesView("title"))

    doc should containElementWithID("common-cases-heading")
  }

  "contain my_cases_secondary_navigation" in {
    val doc = view(commonCasesView("title"))

    doc should containElementWithID("my-cases-secondary-navigation")
  }

}
