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

import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.appeal_details
import uk.gov.tariffclassificationfrontend.utils.Cases._

class AppealDetailsViewSpec extends ViewSpec {

  "Appeal Details" should {

    "Render - Without Appeal" in {
      // Given
      val c = aCase(withDecision(appeal = Seq.empty))

      // When
      val doc = view(appeal_details(c))

      // Then
      for(t <- AppealType.values) {
        doc shouldNot containElementWithID(s"appeal_details-$t")
      }
    }

    "Render - With 'Appeal Allowed'" in {
      // Given
      val c = aCase(withDecision(appeal = Seq(Appeal("id", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-APPEAL_TIER_1")
      doc.getElementById("appeal_details-APPEAL_TIER_1-type") should containText("Appeal tier 1 status")
      doc.getElementById("appeal_details-APPEAL_TIER_1-status") should containText("Appeal allowed")
    }

    "Render - With 'Appeal Dismissed'" in {
      // Given
      val c = aCase(withDecision(appeal = Seq(Appeal("id", AppealStatus.DISMISSED, AppealType.APPEAL_TIER_1))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-APPEAL_TIER_1")
      doc.getElementById("appeal_details-APPEAL_TIER_1-type") should containText("Appeal tier 1 status")
      doc.getElementById("appeal_details-APPEAL_TIER_1-status") should containText("Appeal dismissed")
    }

    "Render - With 'Under Appeal'" in {
      // Given
      val c = aCase(withDecision(appeal = Seq(Appeal("id", AppealStatus.IN_PROGRESS, AppealType.APPEAL_TIER_1))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-APPEAL_TIER_1")
      doc.getElementById("appeal_details-APPEAL_TIER_1-type") should containText("Appeal tier 1 status")
      doc.getElementById("appeal_details-APPEAL_TIER_1-status") should containText("Under appeal")
    }

    "Render - With A Cancel Reason" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.CANCELLED),
        withDecision(cancellation = Some(Cancellation(reason = CancelReason.ANNULLED,  applicationForExtendedUse = true)))
      )

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-extended_use_status")
      doc.getElementById("appeal_details-extended_use_status") should containText("Yes")
    }

    "Render Extended Use Change if user has permission APPEAL_CASE" in {
      // Given
      val c = aCase(withDecision(), withStatus(CaseStatus.CANCELLED))

      // When
      val doc = view(appeal_details(c)(requestWithPermissions(Permission.EXTENDED_USE), messages, appConfig))

      doc should containElementWithID("appeal_details-extended_use-change")
    }

    "Not render Extended Use Change if user does not have permission" in {
      // Given
      val c = aCase(withDecision(), withStatus(CaseStatus.CANCELLED))

      // When
      val doc = view(appeal_details(c)(operatorRequest, messages, appConfig))

      doc shouldNot containElementWithID("appeal_details-extended_use-change")
    }
  }

}
