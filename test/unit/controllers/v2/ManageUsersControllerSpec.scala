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
import models._
import models.viewmodels.{ATaRTab, CorrespondenceTab, LiabilitiesTab, MiscellaneousTab}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.managementtools.manage_users_view
import views.html.v2.open_cases_view

import scala.concurrent.ExecutionContext.Implicits.global

class ManageUsersControllerSpec extends ControllerBaseSpec {

  private lazy val manage_users_view = injector.instanceOf[manage_users_view]

  private def controller(permission: Set[Permission]) = new ManageUsersController(
    new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
    mcc,
    manage_users_view,
    realAppConfig
  )

  "Manage users" should {

    "return 200 OK and HTML content type" in {
      val result = await(controller(Set(Permission.VIEW_CASES)).displayManageUsers()(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).displayManageUsers()(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

}
