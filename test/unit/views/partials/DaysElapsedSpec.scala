/*
 * Copyright 2023 HM Revenue & Customs
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

import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.days_elapsed

class DaysElapsedSpec extends ViewSpec {

  "Days elapsed view" should {

    "render days elapsed in bold when 30 or over" in {

      val doc = view(days_elapsed(30))


      doc                                should containElementWithID("days-elapsed")
      doc.getElementById("days-elapsed") should haveTag("span")
      doc.getElementById("days-elapsed") should containText("30")
      doc.getElementById("days-elapsed") should haveAttribute("class", "extra-bold")
    }

    "render days elapsed with normal font weight when under 30" in {

      val doc = view(days_elapsed(29))


      doc                                should containElementWithID("days-elapsed")
      doc.getElementById("days-elapsed") should haveTag("span")
      doc.getElementById("days-elapsed") should containText("29")
      doc.getElementById("days-elapsed") shouldNot haveAttribute("class", "extra-bold")
    }

    "render days elapsed with a given id" in {

      val doc = view(days_elapsed(5, "special-id"))


      doc should containElementWithID("special-id")
    }

  }
}
