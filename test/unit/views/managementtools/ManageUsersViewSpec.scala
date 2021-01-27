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

package views.managementtools

import models.viewmodels.managementtools.UsersTabViewModel
import models.{Queues}
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.managementtools.manage_users_view

class ManageUsersViewSpec extends ViewSpec {

  def manageUsersView: manage_users_view = injector.instanceOf[manage_users_view]

  "OpenCasesViewSpec" should {

    "render successfully with the default tab" in {
      val doc = view(
        manageUsersView(
          UsersTabViewModel.forManagedTeams(Seq(Queues.cap).toList)
        )
      )
      doc should containElementWithID("manage-users-tabs")
    }
  }

  "contains appropriate queue tabs for teams managed by the manager" in {
    val queues = Queues.allQueues
    val doc = view(
      manageUsersView(
        UsersTabViewModel.forManagedTeams(queues)
      )
    )

    queues.foreach(q => doc should containElementWithID(q.slug.toUpperCase + "-tab"))
  }

  "contain a heading" in {
    val doc = view(
      manageUsersView(
        UsersTabViewModel.forManagedTeams(Seq(Queues.cap).toList)
      )
    )
    doc should containElementWithID("common-cases-heading")
  }

}
