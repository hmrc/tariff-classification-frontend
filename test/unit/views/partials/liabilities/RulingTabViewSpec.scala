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

import controllers.routes
import models.viewmodels.RulingViewModel
import views.ViewSpec
import views.html.partials.liabilities.ruling_tab
import views.ViewMatchers._

class RulingTabViewSpec extends ViewSpec {

  private val rulingViewModel = RulingViewModel(
    "",
    "",
    "123",
    "item description",
    "justification",
    "method search",
    "exclusions",
    true,
    "case reference"
  )

  def rulingTab: ruling_tab = app.injector.instanceOf[ruling_tab]

  "Ruling Tab" should {

    val doc = view(rulingTab(rulingViewModel.copy(showEditRuling = true), 1))
    val notShowEditButton = view(rulingTab(rulingViewModel.copy(showEditRuling = false), 1))

    "display tab title" in {
      doc.getElementById("ruling-heading").text shouldBe messages("case.liability.decision.heading")
    }

    //C592
    "render c592 section name" in {
      doc.getElementsByTag("h3").get(0).text shouldBe messages("case.v2.liability.ruling.info.from.c592")
    }

    "render code by trader row" in {
      val expected = messages("case.v2.liability.ruling.info.from.c592.code.by.trader") + rulingViewModel.commodityCodeEnteredByTraderOrAgent
      doc.getElementById("ruling_code_by_trader").text shouldBe expected
    }

    "render code suggestion" in {
      val expected = messages("case.v2.liability.ruling.info.from.c592.code.suggested") + rulingViewModel.commodityCodeSuggestedByOfficer
      doc.getElementById("ruling_code_suggested").text shouldBe expected
    }

    //liability info
    "render liability details section name" in {
      doc.getElementsByTag("h3").get(1).text shouldBe messages("case.v2.liability.ruling.section")
    }

    "render edit details" in {
      val expected = messages("case.v2.liability.ruling.edit.details")

      doc.getElementById("ruling_edit_details").text shouldBe expected
    }

    "not render edit details" in {
      notShowEditButton.getElementsByTag("a").size() shouldBe 0
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

    "render the correct link for edit details" in {
      doc.getElementById("ruling_edit_details") should containElementWithAttribute("href",routes.RulingController.editRulingDetails(rulingViewModel.caseReference).path())
    }
  }
}
