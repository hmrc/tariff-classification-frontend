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

package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.models.Operator
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.html.assign_case
import uk.gov.tariffclassificationfrontend.utils.Cases._

class AssignCaseViewSpec extends ViewSpec {

  "Assign Case" should {

    "Render link to assign - when currently unassigned" in {
      // Given
      val c = aCase(
        withReference("REF"),
        withoutAssignee()
      )

      // When
      val doc = view(assign_case(c))

      // Then
      doc should containElementWithID("assign_case-unassigned_heading")
      doc shouldNot containElementWithID("assign_case-assigned_heading")

      doc should containElementWithID("assign_case-assign_button")

      doc should containElementWithID("assign_case-continue_button")
      doc.getElementById("assign_case-continue_button") should haveAttribute("href", routes.CaseController.trader("REF").url)
    }

    "Render message - when currently assigned" in {
      // Given
      val c = aCase(
        withReference("REF"),
        withAssignee(Some(Operator("1")))
      )

      // When
      val doc = view(assign_case(c))

      // Then
      doc should containElementWithID("assign_case-assigned_heading")
      doc shouldNot containElementWithID("assign_case-unassigned_heading")

      doc shouldNot containElementWithID("assign_case-assign_button")

      doc should containElementWithID("assign_case-continue_button")
      doc.getElementById("assign_case-continue_button") should haveAttribute("href", routes.CaseController.trader("REF").url)
    }
  }
}
