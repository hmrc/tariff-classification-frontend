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

import models.Permission
import models.forms.KeywordForm
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.keywords_details

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
      doc                                          should containElementWithID("keywords-heading")
      doc                                          should containElementWithID("keywords-row-0-keyword")
      doc.getElementById("keywords-row-0-message") should containText("")
      doc.getElementById("keywords-row-1-message") should containText("Keyword is not from the list")
    }

    "Render a case with keywords with KEYWORDS permissions" in {
      // Given
      val c = aCase().copy(keywords = Set("APPLES", "CARS"))

      // When
      val doc = view(
        keywords_details(c, Seq("APPLES", "TOYS"), KeywordForm.form)(
          requestWithPermissions(Permission.KEYWORDS),
          messages,
          appConfig
        )
      )

      // Then
      doc                                          should containElementWithID("keywords-heading")
      doc                                          should containElementWithID("keywords-row-0-keyword")
      doc.getElementById("keywords-row-0-message") should containText("")
      doc.getElementById("keywords-row-1-message") should containText("Keyword is not from the list")

      doc should containElementWithID("keywords-row-0-remove")
      doc should containElementWithID("keyword_details-add_keyword")
    }

    "Render a case with keywords without KEYWORDS permissions" in {
      // Given
      val c = aCase().copy(keywords = Set("APPLES", "CARS"))

      // When
      val doc = view(keywords_details(c, Seq("APPLES", "TOYS"), KeywordForm.form)(operatorRequest, messages, appConfig))

      // Then
      doc shouldNot containElementWithID("keywords-row-0-remove")
      doc shouldNot containElementWithID("keyword_details-add_keyword")
    }
  }
}
