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

import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import models.Permission
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import utils.Cases
import views.html.v2.common_cases_view

import scala.concurrent.ExecutionContext.Implicits.global

class MyCasesControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  bind[common_cases_view].toInstance(mock[common_cases_view])

  private lazy val common_cases_view = mock[common_cases_view]

  /*private def controller(): MyCasesController = {
    new MyCasesController(
      new RequestActionsWithPermissions(playBodyParsers,
        permissions = Set(Permission.VIEW_MY_CASES),
        c  = Cases.liabilityCaseExample.copy(assignee = Some(Cases.operatorWithPermissions)),
        op = Cases.operatorWithPermissions
      ),
      mcc,
      common_cases_view,
      realAppConfig
    )
  }*/
}
