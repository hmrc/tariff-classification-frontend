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
      val c = aCase(withDecision(appeal = None))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-appeal_status")
      doc.getElementById("appeal_details-appeal_status") should containText("None")
    }

    "Render - With 'Appeal Allowed'" in {
      // Given
      val c = aCase(withDecision(appeal = Some(Appeal(AppealStatus.ALLOWED))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-appeal_status")
      doc.getElementById("appeal_details-appeal_status") should containText("Appeal allowed")
    }

    "Render - With 'Appeal Dismissed'" in {
      // Given
      val c = aCase(withDecision(appeal = Some(Appeal(AppealStatus.DISMISSED))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-appeal_status")
      doc.getElementById("appeal_details-appeal_status") should containText("Appeal dismissed")
    }

    "Render - With 'Under Appeal'" in {
      // Given
      val c = aCase(withDecision(appeal = Some(Appeal(AppealStatus.IN_PROGRESS))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-appeal_status")
      doc.getElementById("appeal_details-appeal_status") should containText("Under appeal")
    }

    "Render - Without Review" in {
      // Given
      val c = aCase(withDecision(review = None))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-review_status")
      doc.getElementById("appeal_details-review_status") should containText("None")
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

    "Render - With 'Review Upheld'" in {
      // Given
      val c = aCase(withDecision(review = Some(Review(ReviewStatus.UPHELD))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-review_status")
      doc.getElementById("appeal_details-review_status") should containText("Review upheld")
    }

    "Render - With 'Review Overturned'" in {
      // Given
      val c = aCase(withDecision(review = Some(Review(ReviewStatus.OVERTURNED))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-review_status")
      doc.getElementById("appeal_details-review_status") should containText("Review overturned")
    }

    "Render - With 'Under Review'" in {
      // Given
      val c = aCase(withDecision(review = Some(Review(ReviewStatus.IN_PROGRESS))))

      // When
      val doc = view(appeal_details(c))

      // Then
      doc should containElementWithID("appeal_details-review_status")
      doc.getElementById("appeal_details-review_status") should containText("Under review")
    }
  }

}
