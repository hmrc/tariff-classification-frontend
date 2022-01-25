/*
 * Copyright 2022 HM Revenue & Customs
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

import models._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.assignee

class AssigneeViewSpec extends ViewSpec {

  "Assignee view" should {

    "Render" in {
      // Given
      val c = Operator("not-your-operator-id", Some("operatorName"))
      // When
      val doc = view(assignee(c))
      // Then
      doc.body should containText("operatorName")
    }

    "Render with capitalize flag true" in {
      // Given
      val operator_0 = Operator("operator-id", Some("operatorName"))
      // When
      val doc = await(view(assignee(operator_0, true)))
      // Then
      doc.body should containText("You")
    }

    "Render with capitalize flag false" in {
      // Given
      val operator_0 = Operator("operator-id", Some("operatorName"))
      // When
      val doc = await(view(assignee(operator_0, false)))
      // Then
      doc.body should containText("you")
    }
  }
}
