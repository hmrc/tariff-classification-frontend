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
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.queue_nav

class QueueNavViewSpec extends ViewSpec with BeforeAndAfterEach {

  override protected def afterEach(): Unit = {
    super.afterEach()
  }

  "Queue Nav" should {

    "Not render team cases if unauthorised" in {
      // Given

      // When
      val doc = view(queue_nav(Seq.empty, ""))

      // Then
      doc shouldNot containElementWithID("nav-menu-assigned-cases")
    }

    "Render team cases if authorised" in {
      // Given

      // When
      val doc = view(queue_nav(Seq.empty, "")(authenticatedManagerFakeRequest, messages))

      // Then
      doc should containElementWithID("nav-menu-assigned-cases")
    }
  }
}
