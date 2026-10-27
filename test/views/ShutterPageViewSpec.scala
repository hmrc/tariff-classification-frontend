/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import views.ViewMatchers._
import views.html.shutterPage

class ShutterPageViewSpec extends ViewSpec {

  "Shutter Page View" should {

    val shutterPageView = app.injector.instanceOf[shutterPage]
    val doc             = view(shutterPageView()(request, messages))

    "render the page title" in {
      doc.title shouldBe s"${messages("shutter.title")} - ${messages("site.title.suffix")}"
    }

    "render the heading" in {
      doc.select("h1").text shouldBe messages("shutter.heading")
    }

    "render the message" in {
      doc should containText(messages("shutter.message"))
    }

    "use the shared GOV.UK layout rather than the legacy GOV.UK template" in {
      doc.select(".govuk-template__body").size                   shouldBe 1
      doc.select("#global-header").size                          shouldBe 0
      doc.select("link[href*=tax.service.gov.uk/template]").size shouldBe 0
    }
  }
}
