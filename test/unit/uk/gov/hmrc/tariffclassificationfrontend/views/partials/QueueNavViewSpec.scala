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

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.tariffclassificationfrontend.models.{Permission, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.queue_nav

class QueueNavViewSpec extends ViewSpec with BeforeAndAfterEach {

  override protected def afterEach(): Unit = {
    super.afterEach()
  }

  val queues: Seq[Queue] = Seq(Queue("1", "act", "ACT"))

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

    "Render queue cases if authorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, "")(requestWithPermissions(Permission.VIEW_QUEUE_CASES), messages))

      // Then
      doc should containElementWithID("nav-menu-queue-act")
    }

    "Not render team cases if unauthorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, ""))

      // Then
      doc shouldNot containElementWithID("nav-menu-assigned-cases")
    }

    "Render team cases if authorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, "")(requestWithPermissions(Permission.VIEW_ASSIGNED_CASES), messages))

      // Then
      doc should containElementWithID("nav-menu-assigned-cases")
    }

    "Not render reports if unauthorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, ""))

      // Then
      doc shouldNot containElementWithID("nav-menu-reports")
    }

    "Render reports if authorised" in {
      // Given

      // When
      val doc = view(queue_nav(queues, "")(requestWithPermissions(Permission.VIEW_REPORTS), messages))

      // Then
      doc should containElementWithID("nav-menu-reports")
    }
  }
}
