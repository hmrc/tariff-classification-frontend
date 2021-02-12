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

import controllers.{ControllerBaseSpec, RequestActions, RequestActionsWithPermissions}
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import service.{CasesService, EventsService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.partials.users._

import scala.concurrent.ExecutionContext.Implicits.global

class ManageUserControllerSpec extends ControllerBaseSpec {

  private lazy val view_user           = injector.instanceOf[view_user]
  private lazy val cannot_delete_user  = injector.instanceOf[cannot_delete_user]
  private lazy val confirm_delete_user = injector.instanceOf[confirm_delete_user]
  private val casesService             = mock[CasesService]
  private val userService              = mock[UserService]

  private def controller(permission: Set[Permission]) =
    new ManageUserController(
      new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
      casesService,
      userService,
      mcc,
      view_user,
      cannot_delete_user,
      confirm_delete_user
    )(realAppConfig, global)

  "Manage user" should {

    "return 200 OK and HTML content type" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Operator("1"))

      val result = await(controller(Set(Permission.MANAGE_USERS)).displayUserDetals("1")(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Operator("1"))

      val result = await(controller(Set()).displayUserDetals("1")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

}
