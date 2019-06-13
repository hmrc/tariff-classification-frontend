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

package uk.gov.hmrc.tariffclassificationfrontend.views

import org.jsoup.select.Elements
import play.twirl.api.Html
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withBTIApplication, withLiabilityApplication, withReference}

class CaseDetailsViewSpec extends ViewSpec {

  "Case Details View" should {

    "render BTI application" in {

      // When
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(html.case_details(c, CaseDetailPage.TRADER, Html("html"), Some("tab-item-Applicant")))

      // Then
      val listItems: Elements = doc.getElementsByClass("tabs__list-item")

      listItems.size() shouldBe 7
      listItems.first should containText("Applicant")
      haveAttribute("aria-selected", "true")
    }

    "render liability order" in {

      // When
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(html.case_details(c, CaseDetailPage.LIABILITY, Html("html"), Some("tab-item-Liability")))

      // Then
      val listItems: Elements = doc.getElementsByClass("tabs__list-item")

      listItems.size() shouldBe 4
      listItems.first should containText("Liability")
      haveAttribute("aria-selected", "true")
    }
  }

}
