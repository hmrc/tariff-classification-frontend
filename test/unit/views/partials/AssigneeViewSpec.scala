/*
 * Copyright 2023 HM Revenue & Customs
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

      val c = Operator("not-your-operator-id", Some("operatorName"))

      val doc = view(assignee(c))

      doc.body should containText("operatorName")
    }

    "Render with capitalize flag true" in {

      val operator_0 = Operator("operator-id", Some("operatorName"))

      val doc = await(view(assignee(operator_0, capitalise = true)))

      doc.body should containText("You")
    }

    "Render with capitalize flag false" in {

      val operator_0 = Operator("operator-id", Some("operatorName"))

      val doc = await(view(assignee(operator_0)))

      doc.body should containText("you")
    }
  }
}
