/*
 * Copyright 2022 HM Revenue & Customs
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

import models._
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.appeal.appeal_details
import models.viewmodels.atar.AppealTabViewModel

class AppealDetailsViewSpec extends ViewSpec {

  "Appeal Details" should {

    "Render - With 'Appeal Allowed'" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.COMPLETED),
        withDecision(appeal = Seq(Appeal("id", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1)))
      )
      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab))

      // Then
      doc                                           should containElementWithID("appeal_details-0")
      doc.getElementById("appeal_details-0-type")   should containText("Appeal tier 1 status")
      doc.getElementById("appeal_details-0-status") should containText("Appeal allowed")
    }

    "Render - With 'Appeal Dismissed'" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.COMPLETED),
        withDecision(appeal = Seq(Appeal("id", AppealStatus.DISMISSED, AppealType.APPEAL_TIER_1)))
      )
      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab))

      // Then
      doc                                           should containElementWithID("appeal_details-0")
      doc.getElementById("appeal_details-0-type")   should containText("Appeal tier 1 status")
      doc.getElementById("appeal_details-0-status") should containText("Appeal dismissed")
    }

    "Render - With 'Under Appeal'" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.COMPLETED),
        withDecision(appeal = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1)))
      )
      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab))

      // Then
      doc                                           should containElementWithID("appeal_details-0")
      doc.getElementById("appeal_details-0-type")   should containText("Appeal tier 1 status")
      doc.getElementById("appeal_details-0-status") should containText("Under appeal")
    }

    "Render - With A Cancel Reason" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.CANCELLED),
        withDecision(cancellation =
          Some(Cancellation(reason = CancelReason.ANNULLED, applicationForExtendedUse = true))
        )
      )

      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab))

      // Then
      doc                                                      should containElementWithID("appeal_details-extended_use_status")
      doc.getElementById("appeal_details-extended_use_status") should containText("Yes")
    }

    "Render Add Appeal if user has permission APPEAL_CASE" in {
      // Given
      val c         = aCase(withDecision(), withStatus(CaseStatus.CANCELLED))
      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab)(requestWithPermissions(Permission.APPEAL_CASE), messages, appConfig))

      doc should containElementWithID("appeal_details-add_new")
    }

    "Not render Add Appeal if user does not have permission" in {
      // Given
      val c         = aCase(withDecision(), withStatus(CaseStatus.CANCELLED))
      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab)(operatorRequest, messages, appConfig))

      doc shouldNot containElementWithID("appeal_details-add_new")
    }

    "Render Change Appeal Status if user has permission APPEAL_CASE" in {
      // Given
      val c = aCase(
        withDecision(appeal = Seq(Appeal("id", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1))),
        withStatus(CaseStatus.CANCELLED)
      )

      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab)(requestWithPermissions(Permission.APPEAL_CASE), messages, appConfig))

      doc should containElementWithID("appeal_details-0-change-status")
    }

    "Not render Change Appeal Status if user does not have permission" in {
      // Given
      val c = aCase(
        withDecision(appeal = Seq(Appeal("id", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1))),
        withStatus(CaseStatus.CANCELLED)
      )

      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab)(operatorRequest, messages, appConfig))

      doc shouldNot containElementWithID("change-status-0")
    }

    "Render Extended Use Change if user has permission EXTENDED_USE" in {
      // Given
      val c         = aCase(withDecision(), withStatus(CaseStatus.CANCELLED))
      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab)(requestWithPermissions(Permission.EXTENDED_USE), messages, appConfig))

      doc should containElementWithID("appeal_details-extended_use-change")
    }

    "Not render Extended Use Change if user does not have permission" in {
      // Given
      val c         = aCase(withDecision(), withStatus(CaseStatus.CANCELLED))
      val appealTab = AppealTabViewModel.fromCase(c).get

      // When
      val doc = view(appeal_details(appealTab)(operatorRequest, messages, appConfig))

      doc shouldNot containElementWithID("appeal_details-extended_use-change")
    }
  }

}
