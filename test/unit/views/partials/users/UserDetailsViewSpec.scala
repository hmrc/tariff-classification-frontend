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

package views.partials.users

import models.{ApplicationType, Operator, Queues, Role}
import views.ViewMatchers.containElementWithID
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.users.user_details

class UserDetailsViewSpec extends ViewSpec {

  def userDetails: user_details = injector.instanceOf[user_details]
  val userWithNoNameAndNoTeam   = Operator("1")
  val userWithAllFields =
    Operator("1", name = Some("Name"), email = Some("Email"), memberOfTeams = Seq(Queues.elm.id))
  "userDetails View" should {
    "render successfully for a classification officer with no teams, no name or email" in {
      val doc = view(
        userDetails(
          userWithNoNameAndNoTeam
        )
      )

      doc should containText("User details")
      doc should containText("Full name")
      doc should containText("PID")
      doc should containText("Email")
      doc should containText("Role")
      doc should containText("Team")
    }

    "render correctly for a classification officer with teams, name and email" in {
      val doc = view(
        userDetails(
          userWithAllFields
        )
      )

      doc should containText("User details")
      doc should containText("Full name")
      doc should containText("PID")
      doc should containText("Email")
      doc should containText("Role")
      doc should containText("Team")
    }
  }

  "render correctly for a manager" in {
    val doc = view(
      userDetails(
        userWithAllFields.copy(role = Role.CLASSIFICATION_MANAGER)
      )
    )

    doc should containText("User details")
    doc should containText("Full name")
    doc should containText("PID")
    doc should containText("Email")
    doc should containText("Role")
    doc should containText("Team")
  }

  "render correctly for a gateway officer" in {
    val doc = view(
      userDetails(
        userWithAllFields.copy(memberOfTeams = Seq(Queues.gateway.id))
      )
    )

    doc should containText("User details")
    doc should containText("Full name")
    doc should containText("PID")
    doc should containText("Email")
    doc should containText("Role")
    doc should containText("Team")
  }

}
