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

package views.components

import views.ViewSpec
import views.html.components.cases_cards

class CaseCardsViewSpec extends ViewSpec{

  def casesCards(countCases: Map[String, Int]) = cases_cards(countCases, 0)



  "Case cards" should {}

  "display the number of cases on My Cases tile when plural" in {

    val countCases = Map("my-cases" -> 2)

    val doc = view(casesCards(countCases))

    doc.getElementById("my-cases-id").text() should include(messages("operator.dashboard.classification.my-cases.progress.plural", 2))

  }

  "display the number of cases on My Cases tile when singular" in {
    val countCases = Map("my-cases" -> 1)

    val doc = view(casesCards(countCases))

    doc.getElementById("my-cases-id").text() should include(messages("operator.dashboard.classification.my-cases.progress.singular", 1))
  }
}
