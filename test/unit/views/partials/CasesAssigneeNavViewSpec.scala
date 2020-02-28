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

package views.partials

import org.scalatestplus.mockito.MockitoSugar
import controllers.routes
import models.Operator
import views.ViewMatchers._
import views.html.partials.cases_assignee_nav
import views.{AssigneeCount, ViewSpec}

class CasesAssigneeNavViewSpec extends ViewSpec with MockitoSugar {

  private def assignedCasesURL(id: String, tabIndex : Int) = routes.AssignedCasesController.assignedCasesFor(id,tabIndex).url

  "Cases Assignee Nav" should {

    "Render empty assignees" in {
      // Given
      val assignees = Seq.empty

      // When
      val doc = view(cases_assignee_nav(assignees, None))

      // Then
      doc should containElementWithID("assignees_list-empty")
    }

    "Render assignees with none selected" in {
      // Given
      val op = Operator("1", Some("Test User"))
      val assignees = Seq(AssigneeCount(op, 1))

      // When
      val doc = view(cases_assignee_nav(assignees, None))

      // Then
      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(1)
      anchors.get(0) should containText("Test User (1)")
      anchors.get(0) should haveAttribute("href", assignedCasesURL("1", 7030))
    }

    "Render assignees with one selected" in {
      // Given
      val op1 = Operator("1", Some("Test User 1"))
      val op2 = Operator("2", Some("Test User 2"))
      val assignees = Seq(AssigneeCount(op1, 1), AssigneeCount(op2, 2))

      // When
      val doc = view(cases_assignee_nav(assignees, Some("1")))

      // Then
      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(2)
      anchors.get(0) should containText("Test User 1 (1)")
      anchors.get(0) should haveAttribute("href", assignedCasesURL("1",7030))

      anchors.get(1) should containText("Test User 2 (2)")
      anchors.get(1) should haveAttribute("href", assignedCasesURL("2",7060))

      val listItems = doc.getElementsByTag("li")
      listItems should haveSize(2)
      listItems.get(0) should haveAttribute("class", "side-nav__list side-nav__list--selected")
      listItems.get(1) should haveAttribute("class", "side-nav__list")
    }
  }
}
