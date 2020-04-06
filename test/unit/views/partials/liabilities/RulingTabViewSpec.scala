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

package views.partials.liabilities

import models.viewmodels.RulingViewModel
import views.ViewMatchers.containText
import views.ViewSpec
import views.html.partials.liabilities.ruling_tab

class RulingTabViewSpec extends ViewSpec {

  def rulingTab: ruling_tab = app.injector.instanceOf[ruling_tab]

  val rulingViewModel = RulingViewModel("123", "item description", "justification", "method search", "exclusions")

  "Ruling Tab" should {

    val doc = view(rulingTab(rulingViewModel, 1))

    "display tab title" in {

      doc should containText(messages("case.liability.decision.heading"))
    }

    "render commodity code" in {

      doc.getElementById("ruling_bindingCommodityCodeValue").text shouldBe rulingViewModel.commodityCode
    }

    "render item description" in {

      doc.getElementById("ruling_itemDescriptionValue").text shouldBe rulingViewModel.itemDescription
    }

    "render justification" in {

      doc.getElementById("ruling_justificationValue").text shouldBe rulingViewModel.justification
    }

    "render searches" in {

      doc.getElementById("ruling_searchesValue").text shouldBe rulingViewModel.methodSearch
    }

    "render exclusions" in {

      doc.getElementById("ruling_exclusionsValue").text shouldBe rulingViewModel.methodExclusion
    }
  }
}
