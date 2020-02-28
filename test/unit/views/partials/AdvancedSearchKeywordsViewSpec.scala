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

import models.forms.SearchForm
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.advanced_search_keywords

class AdvancedSearchKeywordsViewSpec extends ViewSpec {

  "Advanced Search" should {

    "Render Keyword Input" in {
      // When
      val doc = view(advanced_search_keywords(SearchForm.form, Seq.empty))

      // Then
      doc should containElementWithID("keyword_0")
      doc.getElementById("keyword_0") should haveTag("input")
      doc.getElementById("keyword_0") should haveAttribute("value", "")
      doc.getElementById("keyword_0") should haveAttribute("name", "keyword[0]")
    }

    "Not render table if empty" in {
      // When
      val doc = view(advanced_search_keywords(SearchForm.form, Seq.empty))

      // Then
      doc shouldNot containElementWithID("advanced_search_keywords-table")
    }

    "Not render table if keyword blank" in {
      // Given
      val form = SearchForm.form.copy(data = Map(
        "keyword[0]" -> ""
      ))

      // When
      val doc = view(advanced_search_keywords(form, Seq.empty))

      // Then the input box should be blank
      doc should containElementWithID("keyword_0")
      doc.getElementById("keyword_0") should haveTag("input")
      doc.getElementById("keyword_0") should haveAttribute("value", "")
      doc.getElementById("keyword_0") should haveAttribute("name", "keyword[0]")

      // Then the table should not render
      doc shouldNot containElementWithID("advanced_search_keywords-table")
    }

    "Populate Keywords in Table with the input box blank" in {
      // Given
      val form = SearchForm.form.copy(data = Map(
        "keyword[0]" -> "K1"
      ))

      // When
      val doc = view(advanced_search_keywords(form, Seq.empty))

      // Then the input box should be blank
      doc should containElementWithID("keyword_0")
      doc.getElementById("keyword_0") should haveTag("input")
      doc.getElementById("keyword_0") should haveAttribute("value", "")
      doc.getElementById("keyword_0") should haveAttribute("name", "keyword[0]")

      // Then the table should contain the keyword
      doc should containElementWithID("advanced_search_keywords-table")
      doc should containElementWithID("advanced_search_keywords-table-row-1")
      doc should containElementWithID("advanced_search_keywords-table-row-1-input")
      doc.getElementById("advanced_search_keywords-table-row-1-input") should haveTag("input")
      doc.getElementById("advanced_search_keywords-table-row-1-input") should haveAttribute("value", "K1")
      doc.getElementById("advanced_search_keywords-table-row-1-input") should haveAttribute("name", "keyword[1]")
      doc.getElementById("advanced_search_keywords-table-row-1-input") should haveAttribute("type", "hidden")

      doc should containElementWithID("advanced_search_keywords-table-row-1-label")
      doc.getElementById("advanced_search_keywords-table-row-1-label") should containText("K1")

      doc should containElementWithID("advanced_search_keywords-table-row-1-remove_button")
      doc.getElementById("advanced_search_keywords-table-row-1-remove_button") should haveAttribute("type", "button")
      doc.getElementById("advanced_search_keywords-table-row-1-remove_button") should haveAttribute("onclick", "advancedSearch.removeKeyword(1)")
    }

    "Contain autocomplete" in {
      // When
      val doc = view(advanced_search_keywords(SearchForm.form, Seq("K1", "K2")))

      // Then
      doc.html should include("[\"K1\",\"K2\"]")
    }

    "Not contain existing keywords in autocomplete" in {
      // Given
      val form = SearchForm.form.copy(data = Map(
        "keyword[0]" -> "K1"
      ))

      // When
      val doc = view(advanced_search_keywords(form, Seq("K1", "K2")))

      // Then
      doc.html should include("[\"K2\"]")
    }

  }


}
