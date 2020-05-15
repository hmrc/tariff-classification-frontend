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

package views.partials

import play.api.test.FakeRequest
import controllers.SessionKeys
import controllers.SessionKeys.{backToQueuesLinkLabel, backToQueuesLinkUrl}
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.back_link

class BackLinkViewSpec extends ViewSpec {

  "Back link view" should {

    "render back link with details from session" in {
      // Given
      val requestWithSessionData = FakeRequest().withSession((backToQueuesLinkUrl, "url"),
                                                             (backToQueuesLinkLabel, "somewhere nice"))

      // When
      val doc = view(back_link()(requestWithSessionData, messages))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "url")
      doc.getElementById("back-link") should containText("somewhere nice")
    }

    "render back link with default details if session does not contain details" in {
      // When
      val doc = view(back_link())

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "/manage-tariff-classifications")
      doc.getElementById("back-link") should containText(messages("errors.all.back"))
    }

  }
}
