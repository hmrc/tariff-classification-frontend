/*
 * Copyright 2025 HM Revenue & Customs
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

import play.twirl.api.Html
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.progressive_disclosure

class ProgressiveDisclosureViewSpec extends ViewSpec {

  "Progressing Disclosure" should {

    "Render title only when body not present" in {

      val doc = view(progressive_disclosure.render("MODULE", "summary", None, false))

      doc should containElementWithID("MODULE-title")
      doc shouldNot containElementWithID("MODULE-body")
      doc.getElementById("MODULE-title") should containText("summary")
    }
    "Render title only when body not present using .ref.f" in {

      val doc = view(progressive_disclosure.ref.f("MODULE", "summary", None, false))

      doc should containElementWithID("MODULE-title")
      doc shouldNot containElementWithID("MODULE-body")
      doc.getElementById("MODULE-title") should containText("summary")
    }

    "Render drop down when body present" in {

      val doc = view(progressive_disclosure("MODULE", "summary", Some(Html("body"))))

      doc                                should containElementWithID("MODULE-title")
      doc                                should containElementWithID("MODULE-body")
      doc.getElementById("MODULE-title") should containText("summary")
      doc.getElementById("MODULE-body")  should containText("body")
    }

  }

}
