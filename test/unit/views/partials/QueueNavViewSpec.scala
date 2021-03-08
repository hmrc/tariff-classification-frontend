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

package views.partials

import models.{Permission, Queue}
import org.scalatest.BeforeAndAfterEach
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.queue_nav

class QueueNavViewSpec extends ViewSpec with BeforeAndAfterEach {

  override protected def afterEach(): Unit =
    super.afterEach()

  val queues: Seq[Queue] = Seq(Queue("0", "gateway", "Gateway"), Queue("1", "act", "ACT"))

  "Queue Nav" should {

    "Not render my cases if unauthorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, ""))

      // Then
      doc shouldNot containElementWithID("nav-menu-my-cases")
    }

    "Render my cases if authorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, "")(requestWithPermissions(Permission.VIEW_MY_CASES), messages))

      // Then
      doc should containElementWithID("nav-menu-my-cases")
    }

    "Not render queue cases if unauthorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, ""))

      // Then
      doc shouldNot containElementWithID("nav-menu-queue-act")
    }

    "Render separate BTI and liability entries and counts for queue cases" in {
      // Given

      // When
      val doc = view(queue_nav(queues, "")(requestWithPermissions(Permission.VIEW_QUEUE_CASES), messages))

      // Then
      doc should containElementWithID("nav-menu-queue-act")
      doc should containElementWithID("nav-menu-queue-liab-act")
      doc should containElementWithID("case-count-act")
      doc should containElementWithID("liability-count-act")
    }

    "Not render team cases if unauthorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, ""))

      // Then
      doc shouldNot containElementWithID("nav-menu-assigned-cases")
    }

    "Render combined case count for gateway" in {
      // Given
      val queueCounts = Map("-BTI" -> 2, "-LIABILITY_ORDER" -> 2, "2-BTI" -> 3)
      // When
      val doc = view(queue_nav(queues, "", queueCounts)(requestWithPermissions(Permission.VIEW_QUEUE_CASES), messages))

      // Then
      doc.getElementById("case-count-gateway") should containText("4")
    }

    "Render case counts separately for named queue" in {
      // Given
      val queueCounts = Map("-BTI" -> 2, "-LIABILITY_ORDER" -> 2, "1-BTI" -> 3, "1-LIABILITY_ORDER" -> 5)
      // When
      val doc = view(queue_nav(queues, "", queueCounts)(requestWithPermissions(Permission.VIEW_QUEUE_CASES), messages))

      // Then
      doc.getElementById("case-count-act")      should containText("3")
      doc.getElementById("liability-count-act") should containText("5")
    }

  }
}
