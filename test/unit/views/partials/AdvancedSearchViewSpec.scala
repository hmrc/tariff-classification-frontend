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

import play.api.libs.json.JsValue
import models.forms.SearchForm
import models.Paged
import views.ViewMatchers._
import views.{SearchTab, ViewSpec}
import views.html.advanced_search
import utils.Cases._

class AdvancedSearchViewSpec extends ViewSpec {

  private val defaultTab = SearchTab.DETAILS
  private val form = SearchForm.form.bind(Map("any" -> "any"))

  "Advanced Search" should {

    "Render No Results" in {
      // When
      val doc = view(advanced_search(SearchForm.form, None, Seq.empty, defaultTab))

      // Then
      doc shouldNot containElementWithID("advanced_search-results_and_filters")
    }

    "Render Results" in {
      // When
      val doc = view(advanced_search(form, Some(Paged(Seq(SearchResult(aCase(), Seq.empty)))), Seq.empty, defaultTab))

      // Then
      doc should containElementWithID("advanced_search-results_and_filters")
    }

    "Always Render Input 'status'" in {
      view(advanced_search(form, None, Seq.empty, defaultTab)) should containElementWithAttribute("name", "status[0]")
      view(advanced_search(form, None, Seq.empty, defaultTab)) should containElementWithAttribute("name", "status[1]")
      view(advanced_search(form, Some(Paged(Seq(SearchResult(aCase(), Seq.empty)))), Seq.empty, defaultTab)) should containElementWithAttribute("name", "status[0]")
      view(advanced_search(form, Some(Paged(Seq(SearchResult(aCase(), Seq.empty)))), Seq.empty, defaultTab)) should containElementWithAttribute("name", "status[1]")
    }
  }

}
