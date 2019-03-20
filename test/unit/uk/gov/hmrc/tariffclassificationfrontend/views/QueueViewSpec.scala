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

import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Paged, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.tariffclassificationfrontend.utils.Cases

class QueueViewSpec extends ViewSpec {

  "Queue View" should {
    val queue1 = Queue("1", "queue1_name", "Queue 1 Name")
    val queue2 = Queue("2", "queue2_name", "Queue 2 Name")
    val case1 = Cases.btiCaseExample

    "render empty list of cases" in {
      // Given
      val queues = Seq(queue1, queue2)

      // When
      val doc = view(html.queue(queues, queue1, Paged.empty[Case]))

      // Then
      doc should containElementWithID("queue-navigation")
      doc should containElementWithID("queue-name")
      doc should containElementWithID("nav-menu-queue-queue1_name")
      doc should containElementWithID("nav-menu-queue-queue2_name")

      doc should containElementWithID("cases_list-table")

      doc should containElementWithID("nav-menu-my-cases")

      doc should containElementWithID("nav-menu-assigned-cases")

      doc.getElementById("queue-name") should containText("Queue 1 Name Cases")
      doc should containText(messages("cases.table.empty"))
    }

    "render with a list of cases" in {
      // Given
      val queues = Seq(queue1, queue2)
      val cases = Seq(case1)

      // When
      val doc = view(html.queue(queues, queue1, Paged(cases)))

      // Then
      doc should containElementWithID("queue-navigation")
      doc should containElementWithID("queue-name")
      doc should containElementWithID("nav-menu-queue-queue1_name")
      doc should containElementWithID("nav-menu-queue-queue2_name")

      doc should containElementWithID("cases_list-table")

      doc should containElementWithID("nav-menu-my-cases")

      doc should containElementWithID("nav-menu-assigned-cases")

      doc.getElementById("queue-name") should containText("Queue 1 Name")
      doc.getElementById("cases_list-table") should containText(case1.reference)
      doc.getElementById("cases_list-table") should containText(case1.status.toString)
      doc.getElementById("cases_list-table") should containText(case1.application.getType)
    }
  }

}
