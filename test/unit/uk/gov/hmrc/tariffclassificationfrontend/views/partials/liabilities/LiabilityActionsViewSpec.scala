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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.liabilities

import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, _}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.liabilities.liability_actions
import uk.gov.tariffclassificationfrontend.utils.Cases._

class LiabilityActionsViewSpec extends ViewSpec {

  "Liability Actions" should {

    "Render OPEN case with SUSPEND_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityOrderApplication
      )

      // When
      val doc = view(liability_actions(c, 0)(requestWithPermissions(Permission.SUSPEND_CASE), messages, appConfig))

      // Then
      doc should containElementWithID("refer-case-button")
      doc should containElementWithID("reject-case-button")
      doc should containElementWithID("suspend-case-button")

      doc should not (containElementWithID("release-case-button"))
      doc should not (containElementWithID("suppress_link"))
      doc should not (containElementWithID("reopen-case-button"))
    }

    "Render OPEN case without SUSPEND_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityOrderApplication
      )

      // When
      val doc = view(liability_actions(c, 0)(requestWithPermissions(), messages, appConfig))

      // Then
      doc should not (containElementWithID("suspend-case-button"))
    }

    "Render NEW case with RELEASE_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.NEW),
        withLiabilityOrderApplication
      )

      // When
      val doc = view(liability_actions(c, 0)(requestWithPermissions(Permission.RELEASE_CASE), messages, appConfig))

      // Then
      doc should containElementWithID("release-case-button")

      doc should not (containElementWithID("refer-case-button"))
      doc should not (containElementWithID("reject-case-button"))
      doc should not (containElementWithID("suspend-case-button"))
      doc should not (containElementWithID("suppress_link"))
      doc should not (containElementWithID("reopen-case-button"))
    }

    "Render NEW case with SUPPRESS_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.NEW),
        withLiabilityOrderApplication
      )

      // When
      val doc = view(liability_actions(c, 0)(requestWithPermissions(Permission.SUPPRESS_CASE), messages, appConfig))

      // Then
      doc should containElementWithID("suppress_link")

      doc should not (containElementWithID("refer-case-button"))
      doc should not (containElementWithID("reject-case-button"))
      doc should not (containElementWithID("suspend-case-button"))
      doc should not (containElementWithID("release-case-button"))
      doc should not (containElementWithID("reopen-case-button"))
    }

    "Render SUSPENDED case" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.SUSPENDED),
        withLiabilityOrderApplication
      )

      // When
      val doc = view(liability_actions(c, 0)(requestWithPermissions(), messages, appConfig))

      // Then
      doc should containElementWithID("reopen-case-button")

      doc should not (containElementWithID("refer-case-button"))
      doc should not (containElementWithID("reject-case-button"))
      doc should not (containElementWithID("suspend-case-button"))
      doc should not (containElementWithID("release-case-button"))
      doc should not (containElementWithID("suppress_link"))
    }

    "Render REFERRED case" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.REFERRED),
        withLiabilityOrderApplication
      )

      // When
      val doc = view(liability_actions(c, 0)(requestWithPermissions(), messages, appConfig))

      // Then
      doc should containElementWithID("reopen-case-button")

      doc should not (containElementWithID("refer-case-button"))
      doc should not (containElementWithID("reject-case-button"))
      doc should not (containElementWithID("suspend-case-button"))
      doc should not (containElementWithID("release-case-button"))
      doc should not (containElementWithID("suppress_link"))
    }
  }


}
