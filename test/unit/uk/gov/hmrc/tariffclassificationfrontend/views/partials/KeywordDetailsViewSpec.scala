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

import uk.gov.hmrc.tariffclassificationfrontend.forms.KeywordForm
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.keywords_details
import uk.gov.tariffclassificationfrontend.utils.Cases._

class KeywordDetailsViewSpec extends ViewSpec {

  "Keyword Details" should {

    "Render a case with no keywords" in {
      // Given
      val c = aCase()

      // When
      val doc = view(keywords_details(c, Seq("APPLES", "TOYS"), KeywordForm.form))

      // Then
      doc should containElementWithID("keywords-heading")
      doc shouldNot containElementWithID("keywords-row-0-keyword")
    }

    "Render a case with keywords" in {
      // Given
      val c = aCase().copy(keywords = Set("APPLES", "CARS"))

      // When
      val doc = view(keywords_details(c, Seq("APPLES", "TOYS"), KeywordForm.form))

      // Then
      doc should containElementWithID("keywords-heading")
      doc should containElementWithID("keywords-row-0-keyword")
      doc.getElementById("keywords-row-0-message") should containText("")
      doc.getElementById("keywords-row-1-message") should containText("This is not from the main list of keywords")
    }
  }
}
