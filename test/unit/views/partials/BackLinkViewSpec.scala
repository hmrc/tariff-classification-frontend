/*
 * Copyright 2021 HM Revenue & Customs
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
import models.Operator
import models.request.AuthenticatedRequest
import play.api.test.FakeRequest
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.back_link

class BackLinkViewSpec extends ViewSpec {

  "Back link view" should {

    "render back link with details from session" in {
      // When
      val doc = view(back_link()(messages))

      // Then
      doc                             should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "#")
      doc.getElementById("back-link") should containText("Back")
    }

    "render back link with default details if session does not contain details" in {
      // When
      val doc = view(back_link())

      // Then
      doc                             should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "#")
      doc.getElementById("back-link") should containText(messages("errors.all.back"))
    }

    "render back link with default details if session does not contain search label but contains search url" in {
      // When
      val doc = view(back_link()(messages))

      // Then
      doc                             should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "#")
      doc.text()                      shouldBe messages("errors.all.back")
    }

    "render back link with default details if session does not contain queues label but contains queues url" in {
      // When
      val doc = view(back_link()(messages))

      // Then
      doc                             should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "#")
      doc.text()                      shouldBe messages("errors.all.back")
    }

    "render back link with details from session with default text for search result" in {
      // When
      val doc = view(back_link()(messages))

      // Then
      doc                             should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "#")
      doc.text()                      shouldBe "Back"
    }

    "render back link with details from session with custom text for search result" in {
      // When
      val doc = view(back_link()(messages))

      // Then
      doc                             should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "#")
      doc.text()                      shouldBe "Back"
    }

    "render back link with details from session with default text for queues result" in {
      // When
      val doc = view(back_link()(messages))

      // Then
      doc                             should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "#")
      doc.text()                      shouldBe "Back"
    }

    "render back link with details from session with custom text for queues result" in {
      // When
      val doc = view(back_link()(messages))

      // Then
      doc                             should containElementWithID("back-link")
      doc.getElementById("back-link") should haveTag("a")
      doc.getElementById("back-link") should haveAttribute("href", "#")
      doc.text()                      shouldBe "Back"
    }
  }
}
