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
import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import models.Role.Role
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, EventsService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.partials.users.{user_team_edit, view_user}
import views.html.managementtools.manage_users_view
import views.html.partials.users.view_user

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManageUserControllerSpec extends ControllerBaseSpec {

  private lazy val view_user      = injector.instanceOf[view_user]
  private lazy val user_team_edit = injector.instanceOf[user_team_edit]
  private lazy val manage_users_view = injector.instanceOf[manage_users_view]

  private val casesService        = mock[CasesService]
  private val userService         = mock[UserService]
  private val eventService        = mock[EventsService]


  private def controller(permission: Set[Permission]) =
    new ManageUserController(
      new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
      casesService,
      eventService,
      userService,
      mcc,
      view_user,
      user_team_edit,
      manage_users_view
    )(realAppConfig, global)

  "displayUserDetals" should {

    "return 200 OK and HTML content type" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set(Permission.MANAGE_USERS)).displayUserDetails("1")(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return 404 NOT_FOUND and HTML content type when user does not exist" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)

      val result = await(controller(Set(Permission.MANAGE_USERS)).displayUserDetails("1")(fakeRequest))
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "1"))
    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).displayUserDetails("1")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

    "Edit User Team Details" should {

      "return 200 and load the editUserTeamDetails form" in {
        val result = await(
          controller(Set(Permission.MANAGE_USERS))
            .editUserTeamDetails("reference")(newFakePOSTRequestWithCSRF(app))
        )
        status(result) shouldBe OK
      }

      "return unauthorised if the user is not a manager (does not have the right permissions)" in {

        val result = await(
          controller(Set(Permission.VIEW_ASSIGNED_CASES))
            .editUserTeamDetails("reference")(newFakePOSTRequestWithCSRF(app))
        )
        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get should include("unauthorized")
      }
    }

    "Post Edit User Teams" should {

      "redirect to displayUser after user presses 'save changes' button" in {

        when(userService.updateUser(any[Operator], any[Operator])(any[HeaderCarrier])) thenReturn Future(
          Cases.operatorWithPermissions
        )

        val fakeReq = newFakePOSTRequestWithCSRF(
          app,
          Map(
            "memberOfTeams" -> ("2")
          )
        )

        val result = await(
          controller(Set(Permission.MANAGE_USERS))
            .postEditUserTeams("refPID")(fakeReq)
        )

        status(result) shouldBe SEE_OTHER

        locationOf(result) shouldBe Some(
          "/manage-tariff-classifications/users/user/refPID"
        )
      }

      "return to the view if form fails to validate" in {
        when(userService.updateUser(any[Operator], any[Operator])(any[HeaderCarrier])) thenReturn Future(
          Cases.operatorWithPermissions
        )

        val fakeReq = newFakePOSTRequestWithCSRF(
          app,
          Map(
            "" -> ""
          )
        )

        val result = await(
          controller(Set(Permission.MANAGE_USERS))
            .postEditUserTeams("refPID")(fakeReq)
        )

        status(result)           shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/manage-tariff-classifications/users/user/refPID")
      }
    }
  }

  "displayManageUsers" should {

    "return 200 OK and HTML content type" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))
      given(
        casesService.getCasesByAllQueues(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]], any[String])(
          any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))
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
        casesService.getCasesByAllQueues(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]], any[String])(
          any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))
      given(userService.getAllUsers(any[Role], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("2"), Operator("3"))))

      val result = await(controller(Set()).displayManageUsers()(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }
  }
}
