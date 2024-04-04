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

package views.v2

import models.viewmodels.{CaseHeaderViewModel, CaseStatusViewModel, StatusTagViewModel}
import models.{ApplicationType, Contact}
import views.ViewMatchers.{containElementWithClass, containElementWithID, containText}
import views.ViewSpec
import views.html.v2.case_heading

class CaseHeadingViewSpec extends ViewSpec {

  private val caseHeaderViewModel =
    CaseHeaderViewModel(
      ApplicationType.LIABILITY,
      Some("trader-business-name"),
      "good-name",
      "1",
      Some("case-source"),
      Contact("name", "email@email.com"),
      CaseStatusViewModel(None, Some(StatusTagViewModel("OPEN", "red")), None),
      isMigrated = false
    )

  def caseHeading: case_heading = injector.instanceOf[case_heading]

  "Case heading" should {
    "display goods name for liabilities" in {

      val doc = view(
        caseHeading(caseHeaderViewModel)
      )

      doc should containElementWithID("case-reference")

      doc.getElementById("case-reference") should containText("Liability case 1")

    }

    "display goods name for Migrated liabilities if it is present" in {

      val doc = view(
        caseHeading(caseHeaderViewModel.copy(isMigrated = true))
      )

      doc should containElementWithID("case-reference")

      doc.getElementById("case-reference") should containText("Liability case 1")

    }

    "display case reference number for Migrated liabilities if goods name is not present" in {

      val caseHeaderViewModelWithoutGoodsName =
        CaseHeaderViewModel(
          ApplicationType.LIABILITY,
          Some("trader-business-name"),
          "",
          "1",
          Some("case-source"),
          Contact("name", "email@email.com"),
          CaseStatusViewModel(None, Some(StatusTagViewModel("OPEN", "red")), None),
          isMigrated = true
        )

      val doc = view(
        caseHeading(caseHeaderViewModelWithoutGoodsName)
      )

      doc should containElementWithClass("govuk-heading-xl")

      doc should containText("Case 1")
    }

  }

}
