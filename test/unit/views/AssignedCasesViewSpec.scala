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

package views

import models.{Case, Operator, Permission, Queue}
import views.ViewMatchers.{containElementWithID, containText}
import utils.Cases

class AssignedCasesViewSpec extends ViewSpec {

  private val assigneeId = Some("pippo")

  "Assigned Cases View" should {
    val queue1 = Queue("1", "queue1_name", "Queue 1 Name")
    val queue2 = Queue("2", "queue2_name", "Queue 2 Name")
    val case1 = Cases.btiCaseExample

    "render empty list of users" in {
      // Given
      val queues = Seq(queue1, queue2)

      // When
      val doc = view(html.assigned_cases(queues, Seq.empty[Case], assigneeId, Map.empty)(request = requestWithPermissions(Permission.VIEW_QUEUE_CASES, Permission.VIEW_MY_CASES), messages, appConfig))

      // Then
      doc should containElementWithID("queue-navigation")
      doc should containElementWithID("queue-name")
      doc should containElementWithID("nav-menu-queue-queue1_name")
      doc should containElementWithID("nav-menu-queue-queue2_name")
      doc should not(containElementWithID("cases_list-table"))
      doc should containElementWithID("assignee-navigation")
      doc should containElementWithID("assignees_list-empty")
      doc should containElementWithID("nav-menu-my-cases")
      doc should containText(messages("cases.summary.assignee.empty"))
    }

    "render with a list of users " in {
      // Given
      val queues = Seq(queue1, queue2)
      val cases = Seq(case1.copy(assignee = Some(Operator("444", assigneeId))))

      // When
      val doc = view(html.assigned_cases(queues, cases, assigneeId, Map.empty)(request = requestWithPermissions(Permission.VIEW_QUEUE_CASES, Permission.VIEW_MY_CASES), messages, appConfig))

      // Then
      doc should containElementWithID("queue-navigation")
      doc should containElementWithID("queue-name")
      doc should containElementWithID("nav-menu-queue-queue1_name")
      doc should containElementWithID("nav-menu-queue-queue2_name")
      doc should not(containElementWithID("cases_list-table"))
      doc should containElementWithID("nav-menu-my-cases")
      doc should containElementWithID("assignee-navigation")
      doc should not(containElementWithID("assignees_list-empty"))
      doc should containElementWithID("nav-menu-assignee-444")
      doc should not(containText(messages("cases.summary.assignee.empty")))
    }
  }

}
