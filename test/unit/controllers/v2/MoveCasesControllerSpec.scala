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

import akka.stream.Materializer
import connector.DataCacheConnector
import controllers.{ControllerBaseSpec, RequestActionsWithPermissions, RequestActionsWithPermissionsAndData}
import models.Role.Role
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import service.{CasesService, EventsService, QueuesService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.managementtools.manage_users_view
import views.html.partials.users.{user_team_edit, view_user, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class MoveCasesControllerSpec extends ControllerBaseSpec {
  implicit lazy val materializer: Materializer = app.materializer
  private val casesService                     = mock[CasesService]
  private val userService                      = mock[UserService]
  private val queueService                     = mock[QueuesService]
  private val dataCacheConnector               = mock[DataCacheConnector]
  private val teamOrUserPage                   = injector.instanceOf[views.html.partials.users.move_cases_team_or_user]
  private val chooseTeamPage                   = injector.instanceOf[views.html.partials.users.move_cases_choose_team]
  private val chooseTeamToChooseUsersFromPage =
    injector.instanceOf[views.html.partials.users.move_cases_choose_user_team]
  private val chooseUserPage     = injector.instanceOf[views.html.partials.users.move_cases_choose_user]
  private val chooseUserTeamPage = injector.instanceOf[views.html.partials.users.move_cases_choose_one_from_user_teams]
  private val doneMoveCasesPage  = injector.instanceOf[views.html.partials.users.done_move_cases]
  private val viewUser           = injector.instanceOf[views.html.partials.users.view_user]
  private val MoveCasesCacheKey  = "move_cases"
  private val ChosenCases        = "chosen_cases"
  private val ChosenTeam         = "chosen_team"
  private val ChosenUserPID      = "chosen_user_pid"
  private val OriginalUserPID    = "original_user_pid"
  private val userAnswersMock    = UserAnswers(MoveCasesCacheKey)

  private def controller(permission: Set[Permission], userAnswers: UserAnswers = userAnswersMock) =
    new MoveCasesController(
      new RequestActionsWithPermissionsAndData(
        playBodyParsers,
        permission,
        addViewCasePermission = false,
        userAnswers           = userAnswers
      ),
      casesService,
      userService,
      queueService,
      dataCacheConnector,
      mcc,
      teamOrUserPage,
      chooseTeamPage,
      chooseTeamToChooseUsersFromPage,
      chooseUserPage,
      chooseUserTeamPage,
      doneMoveCasesPage,
      viewUser
    )(realAppConfig, global)

  "postMoveATaRCases" should {

    "return 200 OK and HTML content type on form error" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(newFakePOSTRequestWithCSRF(app)))
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("error.moveCases.empty"))

    }

    "return Not found on form error when user not found" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(newFakePOSTRequestWithCSRF(app)))
      status(result) shouldBe Status.NOT_FOUND
    }

    "redirect on valid form" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("atarCases " -> "100")
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(fakeReqWithCases)
        )
      status(result) shouldBe Status.NOT_FOUND //should be see other

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).postMoveATaRCases("1")(newFakeGETRequestWithCSRF(app)))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

  "postMoveLiabilityCases" should {

    "return 200 OK and HTML content type on form error" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.liabilityCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(newFakePOSTRequestWithCSRF(app)))
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("error.moveCases.empty"))

    }

    "return Not found on form error when user not found" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.liabilityCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(newFakePOSTRequestWithCSRF(app)))
      status(result) shouldBe Status.NOT_FOUND
    }

    "redirect on valid form" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.liabilityCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN)
            )
          )
        )

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("liabilityCases " -> "100")
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(fakeReqWithCases)
        )
      status(result) shouldBe Status.NOT_FOUND //should be see other

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.liabilityCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).postMoveLiabCases("1")(newFakeGETRequestWithCSRF(app)))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

  "postMoveCorrespondenceCases" should {

    "return 200 OK and HTML content type on form error" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.correspondenceCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(newFakePOSTRequestWithCSRF(app)))
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("error.moveCases.empty"))

    }

    "return Not found on form error when user not found" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.correspondenceCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(newFakePOSTRequestWithCSRF(app)))
      status(result) shouldBe Status.NOT_FOUND
    }

    "redirect on valid form" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.correspondenceCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN)
            )
          )
        )

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("corrCases " -> "100")
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(fakeReqWithCases)
        )
      status(result) shouldBe Status.NOT_FOUND //should be see other

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.correspondenceCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).postMoveCorrCases("1")(newFakeGETRequestWithCSRF(app)))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

  "postMoveMiscCases" should {

    "return 200 OK and HTML content type on form error" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.miscellaneousCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(
          controller(Set(Permission.MANAGE_USERS), userAnswersMock.set(ChosenCases, Set("100")))
            .postMoveMiscCases("1")(newFakePOSTRequestWithCSRF(app))
        )
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("error.moveCases.empty"))

    }

    "return Not found on form error when user not found" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.miscellaneousCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveMiscCases("1")(newFakePOSTRequestWithCSRF(app)))
      status(result) shouldBe Status.NOT_FOUND
    }

    "redirect on valid form" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.miscellaneousCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN)
            )
          )
        )

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(app).withFormUrlEncodedBody("miscCases " -> "100")
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveMiscCases("1")(fakeReqWithCases)
        )
      status(result) shouldBe Status.NOT_FOUND //should be see other

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.miscellaneousCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).postMoveMiscCases("1")(newFakeGETRequestWithCSRF(app)))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

  "chooseUserOrTeam" should {

    "return 200 OK and HTML content type" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(
          controller(Set(Permission.MANAGE_USERS))
            .chooseUserOrTeam()(newFakeGETRequestWithCSRF(app))
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).chooseUserOrTeam()(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

}
