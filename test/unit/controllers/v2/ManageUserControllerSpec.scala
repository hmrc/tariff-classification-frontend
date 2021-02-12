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
import models.Role.Role
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.managementtools.manage_users_view
import views.html.partials.users.view_user

import scala.concurrent.ExecutionContext.Implicits.global

class ManageUserControllerSpec extends ControllerBaseSpec {

  private lazy val view_user         = injector.instanceOf[view_user]
  private lazy val manage_users_view = injector.instanceOf[manage_users_view]
  private val casesService           = mock[CasesService]
  private val userService            = mock[UserService]

  private def controller(permission: Set[Permission]) =
    new ManageUserController(
      new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
      casesService,
      userService,
      mcc,
      view_user,
      manage_users_view
    )(realAppConfig, global)

  "displayUserDetals" should {

    "return 200 OK and HTML content type" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Operator("1"))

      val result = await(controller(Set(Permission.MANAGE_USERS)).displayUserDetails("1")(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Operator("1"))

      val result = await(controller(Set()).displayUserDetails("1")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }
  }

  "displayManageUsers" should {

    "return 200 OK and HTML content type" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))
      given(
        casesService.getCasesByAllQueues2(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]])(
          any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Operator("1"))
      given(userService.getAllUsers(any[Role], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("2"), Operator("3"))))

      val result = await(controller(Set(Permission.MANAGE_USERS)).displayManageUsers()(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))
      given(
        casesService.getCasesByAllQueues2(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]])(
          any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Operator("1"))
      given(userService.getAllUsers(any[Role], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("2"), Operator("3"))))

      val result = await(controller(Set()).displayManageUsers()(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }
  }
}
