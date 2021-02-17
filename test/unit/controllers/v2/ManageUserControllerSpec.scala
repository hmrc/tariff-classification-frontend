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
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import service.{CasesService, EventsService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.partials.users._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful
import scala.concurrent.Future

class ManageUserControllerSpec extends ControllerBaseSpec {

  private val casesService             = mock[CasesService]
  private val userService              = mock[UserService]
  private val eventService             = mock[EventsService]
  private lazy val user_team_edit      = injector.instanceOf[user_team_edit]
  private lazy val view_user           = injector.instanceOf[view_user]
  private lazy val cannot_delete_user  = injector.instanceOf[cannot_delete_user]
  private lazy val confirm_delete_user = injector.instanceOf[confirm_delete_user]
  private lazy val done_delete_user    = injector.instanceOf[done_delete_user]

  private def controller(permission: Set[Permission]) =
    new ManageUserController(
      new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
      casesService,
      eventService,
      userService,
      mcc,
      user_team_edit,
      view_user,
      cannot_delete_user,
      confirm_delete_user,
      done_delete_user,
      realAppConfig
    )

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

  }

  "deleteUser" should {

    "return 200 OK and HTML content type for a user with cases" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set(Permission.MANAGE_USERS)).deleteUser("1")(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return 200 OK and HTML content type for a user with no cases" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq.empty[Case]))
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set(Permission.MANAGE_USERS)).deleteUser("1")(newFakeGETRequestWithCSRF(app)))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).deleteUser("1")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

  "confirmRemoveUser" should {
    "return 200 and load the delete user form with errors" in {

      val result = await(
        controller(Set(Permission.MANAGE_USERS))
          .confirmRemoveUser("1")(newFakeGETRequestWithCSRF(app))
      )

      status(result) shouldBe OK
    }

  }

  "redirect to manage users when user selects `yes`" in {
    when(userService.updateUser(any[Operator], any[Operator])(any[HeaderCarrier]))
      .thenReturn(successful(Operator("1", deleted = true)))

    val result: Result = await(
      controller(Set(Permission.MANAGE_USERS))
        .confirmRemoveUser("1")(
          newFakePOSTRequestWithCSRF(app)
            .withFormUrlEncodedBody("state" -> "true")
        )
    )

    redirectLocation(result) shouldBe Some(routes.ManageUserController.doneDeleteUser("PID 1").path)
  }

  "redirect to manage user when user selects `no`" in {
    val result: Result = await(
      controller(Set(Permission.MANAGE_USERS))
        .confirmRemoveUser("1")(
          newFakePOSTRequestWithCSRF(app)
            .withFormUrlEncodedBody("state" -> "false")
        )
    )

    redirectLocation(result) shouldBe Some(routes.ManageUserController.displayUserDetails("1").path)
  }

  "doneDeleteUser" should {

    "return 200 OK and HTML content type" in {
      val result = await(controller(Set(Permission.MANAGE_USERS)).doneDeleteUser("1")(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).doneDeleteUser("1")(fakeRequest))
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

}
