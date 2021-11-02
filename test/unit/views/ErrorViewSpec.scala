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

package views

import views.ViewMatchers._

class ErrorViewSpec extends ViewSpec {

  "Error View" should {

    "render empty list of cases" in {

      // When
      val doc = view(html.error_template("Title", "Heading", "Message"))

      // Then
      doc should containText("Title")
      doc should containText("Heading")
      doc should containText("Message")
    }
  }

}
