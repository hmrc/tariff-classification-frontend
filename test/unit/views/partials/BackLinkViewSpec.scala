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

import controllers.SessionKeys._
import play.api.test.FakeRequest
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.back_link

class BackLinkViewSpec extends ViewSpec {

  "Back link view" should {

    "render back link with details from session" in {
      // Given
      val requestWithSessionData = FakeRequest().withSession(
        (backToQueuesLinkUrl, "url"),
        (backToQueuesLinkLabel, "somewhere nice")
      )

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

    "render back link with default details if session does not contain search label but contains search url" in {
      // Given
      val requestWithSessionData = FakeRequest().withSession(
        (backToSearchResultsLinkUrl, "url")
      )

      // When
      val doc = view(back_link()(requestWithSessionData, messages))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "url")
      doc.text() shouldBe messages("errors.all.back")
    }

    "render back link with default details if session does not contain queues label but contains queues url" in {
      // Given
      val requestWithSessionData = FakeRequest().withSession(
        (backToQueuesLinkUrl, "url")
      )

      // When
      val doc = view(back_link()(requestWithSessionData, messages))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "url")
      doc.text() shouldBe messages("errors.all.back")
    }

    "render back link with details from session with default text for search result" in {
      // Given
      val requestWithSessionData = FakeRequest().withSession(
        (backToSearchResultsLinkUrl, "url"),
        (backToSearchResultsLinkLabel, "")
      )

      // When
      val doc = view(back_link()(requestWithSessionData, messages))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "url")
      doc.text() shouldBe "Back"
    }

    "render back link with details from session with custom text for search result" in {
      val customLabel = "custom"
      // Given
      val requestWithSessionData = FakeRequest().withSession(
        (backToSearchResultsLinkUrl, "url"),
        (backToSearchResultsLinkLabel, customLabel)
      )

      // When
      val doc = view(back_link()(requestWithSessionData, messages))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "url")
      doc.text() shouldBe "Back to " + customLabel
    }

    "render back link with details from session with default text for queues result" in {
      // Given
      val requestWithSessionData = FakeRequest().withSession(
        (backToQueuesLinkUrl, "url"),
        (backToQueuesLinkLabel, "")
      )

      // When
      val doc = view(back_link()(requestWithSessionData, messages))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "url")
      doc.text() shouldBe "Back"
    }

    "render back link with details from session with custom text for queues result" in {
      val customLabel = "custom"
      // Given
      val requestWithSessionData = FakeRequest().withSession(
        (backToQueuesLinkUrl, "url"),
        (backToQueuesLinkLabel, customLabel)
      )

      // When
      val doc = view(back_link()(requestWithSessionData, messages))

      // Then
      doc should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "url")
      doc.text() shouldBe "Back to " + customLabel
    }
  }
}
