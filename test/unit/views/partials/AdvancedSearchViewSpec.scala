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

import models.Paged
import models.forms.SearchForm
import utils.Cases._
import views.ViewMatchers._
import views.html.advanced_search
import views.{SearchTab, ViewSpec}

class AdvancedSearchViewSpec extends ViewSpec {

  private val defaultTab = SearchTab.DETAILS
  private val form       = SearchForm.form.bind(Map("any" -> "any"))
  val advancedSearchView = app.injector.instanceOf[advanced_search]

  "Advanced Search" should {

    "Render No Results" in {
      // When
      val doc = view(advancedSearchView(SearchForm.form, None, Seq.empty, defaultTab))

      // Then
      doc shouldNot containElementWithID("advanced_search-results_and_filters")
    }

    "Render Results" in {
      // When
      val doc = view(advancedSearchView(form, Some(Paged(Seq(SearchResult(aCase(), Seq.empty)))), Seq.empty, defaultTab))

      // Then
      doc should containElementWithID("advanced_search-results_and_filters")
    }

    "Always Render Input 'status'" in {
      view(advancedSearchView(form, None, Seq.empty, defaultTab)) should containElementWithAttribute("name", "status[0]")
      view(advancedSearchView(form, None, Seq.empty, defaultTab)) should containElementWithAttribute("name", "status[1]")
      view(advancedSearchView(form, Some(Paged(Seq(SearchResult(aCase(), Seq.empty)))), Seq.empty, defaultTab)) should containElementWithAttribute(
        "name",
        "status[0]"
      )
      view(advancedSearchView(form, Some(Paged(Seq(SearchResult(aCase(), Seq.empty)))), Seq.empty, defaultTab)) should containElementWithAttribute(
        "name",
        "status[1]"
      )
    }
  }

}
