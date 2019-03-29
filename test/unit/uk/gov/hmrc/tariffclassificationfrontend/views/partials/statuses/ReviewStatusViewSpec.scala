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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.statuses

import uk.gov.hmrc.tariffclassificationfrontend.models.{Review, ReviewStatus}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.statuses.review_status
import uk.gov.tariffclassificationfrontend.utils.Cases
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withDecision, withReference}

class ReviewStatusViewSpec extends ViewSpec {

  "Review Status" should {

    "render the review status if it is defined" in {
      // When
      val c = aCase(
        withReference("ref"),
        withDecision(review = Some(Review(ReviewStatus.OVERTURNED)))
      )

      val doc = view(review_status(c, "id"))

      // Then
      doc.text() shouldBe "Review overturned"
    }

    "not render the review status if it is not defined" in {
      // When
      val doc = view(review_status(Cases.btiCaseExample, "id"))

      // Then
      doc.text() shouldBe ""
    }

  }

}
