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

import uk.gov.hmrc.tariffclassificationfrontend.forms.SearchForm
import uk.gov.hmrc.tariffclassificationfrontend.models.Search
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.advanced_search
import uk.gov.tariffclassificationfrontend.utils.Cases._

class AdvancedSearchViewSpec extends ViewSpec {

  "Advanced Search" should {

    "Render No Results" in {
      // When
      val doc = view(advanced_search(SearchForm.form, None, Seq.empty))

      // Then
      doc shouldNot containElementWithID("advanced_search-results_and_filters")
    }

    "Render Results" in {
      // Given
      val c = aCase()

      // When
      val doc = view(advanced_search(SearchForm.form, Some(Seq(c)), Seq.empty))

      // Then
      doc should containElementWithID("advanced_search-results_and_filters")
    }

    "Always Render Input 'live_rulings_only'" in {
      view(advanced_search(SearchForm.form, None, Seq.empty)) should containElementWithAttribute("name", "live_rulings_only")
      view(advanced_search(SearchForm.form, Some(Seq(aCase())), Seq.empty)) should containElementWithAttribute("name", "live_rulings_only")
    }
  }

}
