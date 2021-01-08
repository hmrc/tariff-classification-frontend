/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{Case, Paged, Permission, Queue}
import utils.Cases
import views.ViewMatchers._

class QueueViewSpec extends ViewSpec {

  "Queue View" should {
    val queue1   = Queue("1", "queue1_name", "Queue 1 Name")
    val queue2   = Queue("2", "queue2_name", "Queue 2 Name")
    val case1    = Cases.btiCaseExample
    val liabCase = Cases.liabilityCaseExample

    "render create liability button when user has CREATE_CASES permission" in {
      // Given
      val queues = Seq(queue1, queue2)

      // When
      val doc = view(
        html.queue(queues, queue1, Map.empty, Paged.empty[Case], "BTI,LIABILITY")(
          request = requestWithPermissions(Permission.CREATE_CASES),
          messages,
          appConfig
        )
      )

      // Then
      doc should containElementWithID("create_liability-button")
    }

    "render page with title applicable to selected queue" in {
      val queues = Seq(queue1, queue2)

      val doc = view(
        html.queue(queues, queue1, Map.empty, Paged.empty[Case], "BTI")(
          request = requestWithPermissions(Permission.CREATE_CASES),
          messages,
          appConfig
        )
      )

      doc should containHtml(
        "<title>page.title.case.queues.queue1_name cases - Advance Tariff Rulings Case Manager - GOV.UK</title>"
      )
    }

    "render page with title applicable to selected liability queue" in {
      val queues = Seq(queue1, queue2)

      val doc = view(
        html.queue(queues, queue1, Map.empty, Paged.empty[Case], "LIABILITY")(
          request = requestWithPermissions(Permission.CREATE_CASES),
          messages,
          appConfig
        )
      )

      doc should containHtml(
        "<title>page.title.case.queues.queue1_name-liab cases - Advance Tariff Rulings Case Manager - GOV.UK</title>"
      )
    }

    "not render create liability button when user does not have permission" in {
      // Given
      val queues = Seq(queue1, queue2)

      // When
      val doc = view(
        html.queue(queues, queue1, Map.empty, Paged.empty[Case], "BTI,LIABILITY_ORDER")(
          request = requestWithPermissions(),
          messages,
          appConfig
        )
      )

      // Then
      doc should not(containElementWithID("create_liability-button"))
    }

    "render empty list of cases" in {
      // Given
      val queues = Seq(queue1, queue2)

      // When
      val doc = view(
        html.queue(queues, queue1, Map.empty, Paged.empty[Case], "BTI,LIABILITY_ORDER")(
          request = requestWithPermissions(Permission.VIEW_QUEUE_CASES, Permission.VIEW_MY_CASES),
          messages,
          appConfig
        )
      )

      // Then
      doc should containElementWithID("queue-navigation")
      doc should containElementWithID("queue-name")
      doc should containElementWithID("nav-menu-queue-queue1_name")
      doc should containElementWithID("nav-menu-queue-queue2_name")

      doc shouldNot containElementWithID("cases_list-table")

      doc should containElementWithID("nav-menu-my-cases")

      doc.getElementById("queue-name") should containText("page.title.case.queues.queue1_name cases")
      doc                              should containText(messages("cases.table.empty"))
    }

    "render with a list of cases" in {
      // Given
      val queues = Seq(queue1, queue2)
      val cases  = Seq(case1)

      // When
      val doc = view(
        html.queue(queues, queue1, Map.empty, Paged(cases), "BTI,LIABILITY_ORDER")(
          request = requestWithPermissions(Permission.VIEW_QUEUE_CASES, Permission.VIEW_MY_CASES),
          messages,
          appConfig
        )
      )

      // Then
      doc should containElementWithID("queue-navigation")
      doc should containElementWithID("queue-name")
      doc should containElementWithID("nav-menu-queue-queue1_name")
      doc should containElementWithID("nav-menu-queue-queue2_name")

      doc should containElementWithID("cases_list-table")

      doc should containElementWithID("nav-menu-my-cases")

      doc.getElementById("queue-name")       should containText("page.title.case.queues.queue1_name")
      doc.getElementById("cases_list-table") should containText(case1.reference)
      doc.getElementById("cases_list-table") should containText(case1.status.toString)
      doc.getElementById("cases_list-table") should containText(case1.application.getType)
    }

    "render both BTI and Liability" in {
      // Given
      val queues = Seq(queue1, queue2)
      val cases  = Seq(case1, liabCase)

      // When
      val doc = view(
        html.queue(queues, queue1, Map.empty, Paged(cases), "BTI,LIABILITY_ORDER")(
          request = requestWithPermissions(Permission.VIEW_QUEUE_CASES, Permission.VIEW_MY_CASES),
          messages,
          appConfig
        )
      )

      // Then
      doc.getElementById("cases_list-row-0-type") should containText("ATaR")
      doc.getElementById("cases_list-row-1-type") should containText("Liability")
    }

  }

}
