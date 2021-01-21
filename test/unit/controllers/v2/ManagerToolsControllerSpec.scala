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

package controllers.v2

import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import models.Permission
import models.viewmodels.ManagerToolsReportsTab
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import views.html.v2.manager_tools_view

import scala.concurrent.ExecutionContext.Implicits.global
class ManagerToolsControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private lazy val managerToolsView = injector.instanceOf[manager_tools_view]

  private def controller(permission: Set[Permission]) = new ManagerToolsController(
    new RequestActionsWithPermissions(playBodyParsers, permission),
    mcc,
    managerToolsView,
    realAppConfig
  )

  "MyCasesController" should {

    "return 200 and the correct content when no tab has ben specified" in {
      val result = await(controller(Set(Permission.VIEW_REPORTS))).displayManagerTools()(fakeRequest)

      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      status(result)      shouldBe Status.OK
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).displayManagerTools()(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER

      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)
    }

    "return 200 OK with the correct subNavigation tab for ManagerToolsReportsTab" in {

      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).displayManagerTools(ManagerToolsReportsTab)(fakeRequest))

      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      status(result)      shouldBe Status.OK
    }
  }

}
