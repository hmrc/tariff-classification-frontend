/*
 * Copyright 2024 HM Revenue & Customs
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

import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.components.cases_cards

class CaseCardsViewSpec extends ViewSpec {

  "Case cards" should {

    "display the name of the operator if present" in {
      val doc = view(cases_cards(Map.empty, 2, 0, 0)(operatorRequestWithName, messages, appConfig))

      doc.getElementsByClass("govuk-heading-xl").text() should include(
        "Case dashboard"
      )
    }

    "display the correct title if the operator is a classification-officer" in {

      val doc = view(cases_cards(casesByTeam = Map.empty, 2, 0, 0)(operatorRequestWithName, messages, appConfig))

      doc.getElementsByClass("govuk-heading-xl").text() should include(
        "Case dashboard"
      )
    }

    "display the correct title if the operator is a classification-manager" in {

      val doc =
        view(cases_cards(Map.empty, 2, 0, 0)(authenticatedManagerFakeRequest, messages, appConfig))

      doc.getElementsByClass("govuk-heading-xl").text() should include(
        "Case dashboard"
      )
    }

    "display the number of cases on My Cases tile when plural" in {
      val doc = view(cases_cards(Map.empty, 2, 0, 0))

      doc.getElementById("my-cases-id").text() should include(
        messages("operator.dashboard.classification.my-cases.progress.plural", 2)
      )

    }

    "display the number of cases on My Cases tile when singular" in {
      val doc = view(cases_cards(Map.empty, 1, 0, 0))

      doc.getElementById("my-cases-id").text() should include(
        messages("operator.dashboard.classification.my-cases.progress.singular", 1)
      )
    }

    "display the number of cases on My Referred Cases tile when plural" in {
      val doc = view(cases_cards(Map.empty, 0, 2, 0)(operatorRequestWithName, messages, appConfig))

      doc.getElementById("my-referred-cases-id").text() should include(
        messages("operator.dashboard.classification.my-cases.onReferralProgress.plural", 2)
      )
    }

    "display the number of cases on My Referred Cases tile when singular" in {
      val doc = view(cases_cards(Map.empty, 0, 1, 0)(operatorRequestWithName, messages, appConfig))

      doc.getElementById("my-referred-cases-id").text() should include(
        messages("operator.dashboard.classification.my-cases.onReferralProgress.singular", 1)
      )
    }

    "display the number of cases on My Completed Cases tile when plural" in {
      val doc = view(cases_cards(Map.empty, 0, 0, 2)(operatorRequestWithName, messages, appConfig))

      doc.getElementById("my-completed-cases-id").text() should include(
        messages("operator.dashboard.classification.my-cases.onCompletedProgress.plural", 2)
      )
    }

    "display the number of cases on My Complete Cases tile when singular" in {
      val doc = view(cases_cards(Map.empty, 0, 0, 1)(operatorRequestWithName, messages, appConfig))

      doc.getElementById("my-completed-cases-id").text() should include(
        messages("operator.dashboard.classification.my-cases.onCompletedProgress.singular", 1)
      )
    }

    "display the manager tools, my cases, open cases and gateway" in {
      val doc =
        view(cases_cards(Map.empty, 2, 0, 0)(authenticatedManagerFakeRequest, messages, appConfig))

      doc should containElementWithID("my-referred-cases-id")

      doc should containElementWithID("my-cases-id")
      doc should containElementWithID("bti-cases-id")

      doc should containElementWithID("manager-tools-users-id")
      doc should containElementWithID("manager-tools-keywords-id")
      doc should containElementWithID("manager-tools-reports-id")

      doc should containElementWithID("gateway-cases-id")
    }
  }
}
