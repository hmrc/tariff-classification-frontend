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

package views.partials.liabilities

import models.{CaseStatus, _}
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.liabilities.liability_actions

class LiabilityActionsViewSpec extends ViewSpec {

  "Liability Actions" should {

    def containHeading = containElementWithTag("h3")
    def containSuspend = containElementWithID("suspend-case-button")
    def containRefer = containElementWithID("refer-case-button")
    def containReject = containElementWithID("reject-case-button")
    def containRelease = containElementWithID("release-case-button")
    def containSuppress = containElementWithID("suppress_link")
    def containReopen = containElementWithID("reopen-case-button")

    "Render case with no permissions" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication()
      )

      // When
      val doc = view(liability_actions(c)(requestWithPermissions(), messages, appConfig))

      // Then
      doc shouldNot containHeading
      doc shouldNot containSuspend
      doc shouldNot containRefer
      doc shouldNot containReject
      doc shouldNot containRelease
      doc shouldNot containSuppress
      doc shouldNot containReopen
    }

    "Render OPEN case with SUSPEND_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication()
      )

      // When
      val doc = view(liability_actions(c)(requestWithPermissions(Permission.SUSPEND_CASE), messages, appConfig))

      // Then
      doc should containHeading
      doc should containSuspend

      doc shouldNot containRefer
      doc shouldNot containReject
      doc shouldNot containRelease
      doc shouldNot containSuppress
      doc shouldNot containReopen
    }

    "Render OPEN case with REJECT_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication()
      )

      // When
      val doc = view(liability_actions(c)(requestWithPermissions(Permission.REJECT_CASE), messages, appConfig))

      // Then
      doc should containHeading
      doc should containReject

      doc shouldNot containSuspend
      doc shouldNot containRefer
      doc shouldNot containRelease
      doc shouldNot containSuppress
      doc shouldNot containReopen
    }

    "Render OPEN case without SUSPEND_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication()
      )

      // When
      val doc = view(liability_actions(c)(requestWithPermissions(), messages, appConfig))

      // Then
      doc shouldNot containSuspend
    }

    "Render NEW case with RELEASE_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.NEW),
        withLiabilityApplication()
      )

      // When
      val doc = view(liability_actions(c)(requestWithPermissions(Permission.RELEASE_CASE), messages, appConfig))

      // Then
      doc should containHeading
      doc should containRelease

      doc shouldNot containRefer
      doc shouldNot containReject
      doc shouldNot containSuspend
      doc shouldNot containSuppress
      doc shouldNot containReopen
    }

    "Render NEW case with SUPPRESS_CASE permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.NEW),
        withLiabilityApplication()
      )

      // When
      val doc = view(liability_actions(c)(requestWithPermissions(Permission.SUPPRESS_CASE), messages, appConfig))

      // Then
      doc should containHeading
      doc should containSuppress

      doc shouldNot containRefer
      doc shouldNot containReject
      doc shouldNot containSuspend
      doc shouldNot containRelease
      doc shouldNot containReopen
    }

    "Render SUSPENDED case with REOPEN permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.SUSPENDED),
        withLiabilityApplication()
      )

      // When
      val doc = view(liability_actions(c)(requestWithPermissions(Permission.REOPEN_CASE), messages, appConfig))

      // Then
      doc should containHeading
      doc should containReopen

      doc shouldNot containRefer
      doc shouldNot containReject
      doc shouldNot containSuspend
      doc shouldNot containRelease
      doc shouldNot containSuppress
    }

    "Render REFERRED case with REOPEN permission" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.REFERRED),
        withLiabilityApplication()
      )

      // When
      val doc = view(liability_actions(c)(requestWithPermissions(Permission.REOPEN_CASE), messages, appConfig))

      // Then
      doc should containHeading
      doc should containReopen

      doc shouldNot containRefer
      doc shouldNot containReject
      doc shouldNot containSuspend
      doc shouldNot containRelease
      doc shouldNot containSuppress
    }
  }


}
