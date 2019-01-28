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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import play.api.mvc.Call
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.case_nav_item

class CaseNavItemViewSpec extends ViewSpec {

  "Case Nav Item" should {

    "Render anchor when inactive" in {
      // When
      val doc = view(case_nav_item(active = false, title = "title", Call("GET", "url")))

      // Then
      doc should containElementWithTag("a")
      doc shouldNot containElementWithTag("span")
      val anchor = doc.getElementsByTag("a").first()
      anchor should containText("title")
      anchor should haveAttribute("href", "url")
    }

    "Render text when active" in {
      // When
      val doc = view(case_nav_item(active = true, title = "title", Call("GET", "url")))

      // Then
      doc shouldNot containElementWithTag("a")
      doc should containElementWithTag("span")
      val span = doc.getElementsByTag("span").first()
      span should containText("title")
    }
  }

}
