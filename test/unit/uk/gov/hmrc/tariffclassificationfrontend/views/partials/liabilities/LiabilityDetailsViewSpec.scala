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
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.liability_details
import uk.gov.tariffclassificationfrontend.utils.Cases._

class LiabilityDetailsViewSpec extends ViewSpec {

  "Liability  Details" should {

    "Not render edit button if not permitted" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication
      )

      // When
      val doc = view(liability_details(c)(requestWithPermissions(), messages, appConfig))

      // Then
      doc shouldNot containElementWithID("liability-decision-edit")
    }

    "Render edit button if permitted" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication
      )

      // When
      val doc = view(liability_details(c)(requestWithPermissions(Permission.EDIT_RULING), messages, appConfig))

      // Then
      doc should containElementWithID("liability-decision-edit")
    }

    "Not render liability details if empty" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication,
        withoutDecision()
      )

      // When
      val doc = view(liability_details(c)(requestWithPermissions(), messages, appConfig))

      // Then
      doc shouldNot containElementWithID("liability_details-decision")
    }

    "Render liability details if present" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication,
        withDecision(
          bindingCommodityCode = "code",
          justification = "justification",
          goodsDescription = "description",
          methodSearch = Some("search"),
          methodExclusion = Some("exclusion")
        )
      )

      // When
      val doc = view(liability_details(c)(requestWithPermissions(), messages, appConfig))

      // Then
      doc should containElementWithID("liability_details-decision")
      doc.getElementById("liability-decision-code") should containText("code")
      doc.getElementById("liability-decision-description") should containText("description")
      doc.getElementById("liability-decision-justification") should containText("justification")
      doc.getElementById("liability-decision-searches") should containText("search")
      doc.getElementById("liability-decision-exclusions") should containText("exclusion")
    }

  }

}
