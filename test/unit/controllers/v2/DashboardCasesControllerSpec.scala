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

package controllers.v2

import controllers.ControllerBaseSpec
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import views.html.v2.assigned_cases_v2

class DashboardCasesControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {
  bind[assigned_cases_v2].toInstance(mock[assigned_cases_v2])

  private lazy val assignedCases_view = bind[assigned_cases_v2]

  private def controller(): DashboardCasesController = {
    new DashboardCasesController(
      new RequestActionsWithPermissionsProvider(),
      mcc,
      assignedCases_view,
      realAppConfig
    )
  }
}
