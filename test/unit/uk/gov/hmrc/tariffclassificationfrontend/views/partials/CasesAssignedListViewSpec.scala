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

import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.cases_assigned_list
import uk.gov.hmrc.tariffclassificationfrontend.views.{AssignedCases, ViewSpec}
import uk.gov.tariffclassificationfrontend.utils.Cases._

class CasesAssignedListViewSpec extends ViewSpec {

  "Cases Assigned List" should {

    "Render nothing" in {
      // Given
      val assignedCases = None

      // When
      val doc = view(cases_assigned_list(assignedCases))

      // Then
      doc should containElementWithID("assignees_list-empty")
    }

    "Render some" in {
      // Given
      val openCase = aCase(
        withReference("REF1"),
        withStatus(CaseStatus.OPEN),
        withDaysElapsed(2)
      )

      val referredCase = aCase(
        withReference("REF2"),
        withStatus(CaseStatus.REFERRED),
        withDaysElapsed(1)
      )

      val assignedCases = Some(AssignedCases("User Name", Seq(openCase), Seq(referredCase)))

      // When
      val doc = view(cases_assigned_list(assignedCases))

      // Then
      doc shouldNot containElementWithID("assignees_list-empty")

      doc should containElementWithID("cases_list-open-row-0")
      doc should containElementWithID("cases_list-open-row-0-reference")
      doc.getElementById("cases_list-open-row-0-reference") should containText("REF1")
      doc should containElementWithID("cases_list-open-row-0-status")
      doc.getElementById("cases_list-open-row-0-status") should containText("OPEN")
      doc should containElementWithID("cases_list-open-row-0-days_elapsed")
      doc.getElementById("cases_list-open-row-0-days_elapsed") should containText("2")
      doc.getElementById("cases_list-open-row-0-reference") should haveAttribute("href", routes.CaseController.trader("REF1").url)
      doc.getElementById("cases_list-open-row-0-move") should haveAttribute("href", routes.ReassignCaseController.showAvailableQueues("REF1").url)

      doc should containElementWithID("cases_list-other-row-0")
      doc should containElementWithID("cases_list-other-row-0-reference")
      doc.getElementById("cases_list-other-row-0-reference") should containText("REF2")
      doc should containElementWithID("cases_list-other-row-0-status")
      doc.getElementById("cases_list-other-row-0-status") should containText("REFERRED")
      doc should containElementWithID("cases_list-other-row-0-days_elapsed")
      doc.getElementById("cases_list-other-row-0-days_elapsed") should containText("1")
      doc.getElementById("cases_list-other-row-0-reference") should haveAttribute("href", routes.CaseController.trader("REF2").url)
      doc.getElementById("cases_list-other-row-0-move") should haveAttribute("href", routes.ReassignCaseController.showAvailableQueues("REF2").url)
    }
  }
}
