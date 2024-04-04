/*
 * Copyright 2024 HM Revenue & Customs
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

import connector.DataCacheConnector
import controllers.{ControllerBaseSpec, RequestActionsWithPermissions, RequestActionsWithPermissionsAndData}
import models.Role.Role
import models._
import models.forms.v2._
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import models.cache.CacheMap
import utils.Cases

class MoveCasesControllerSpec extends ControllerBaseSpec {
  private val casesService       = mock[CasesService]
  private val userService        = mock[UserService]
  private val queueService       = mock[QueuesService]
  private val dataCacheConnector = mock[DataCacheConnector]
  private val teamOrUserPage     = injector.instanceOf[views.html.partials.users.move_cases_team_or_user]
  private val chooseTeamPage     = injector.instanceOf[views.html.partials.users.move_cases_choose_team]
  private val chooseTeamToChooseUsersFromPage =
    injector.instanceOf[views.html.partials.users.move_cases_choose_user_team]
  private val chooseUserPage     = injector.instanceOf[views.html.partials.users.move_cases_choose_user]
  private val chooseUserTeamPage = injector.instanceOf[views.html.partials.users.move_cases_choose_one_from_user_teams]
  private val doneMoveCasesPage  = injector.instanceOf[views.html.partials.users.done_move_cases]
  private val viewUser           = injector.instanceOf[views.html.partials.users.view_user]
  private val userNotFound       = injector.instanceOf[views.html.user_not_found]
  private val resourceNotFound   = injector.instanceOf[views.html.resource_not_found]
  private val MoveCasesCacheKey  = "move_cases"
  private val ChosenCases        = "chosen_cases"
  private val ChosenTeam         = "chosen_team"
  private val ChosenUserPID      = "chosen_user_pid"
  private val OriginalUserPID    = "original_user_pid"
  private val userAnswersMock    = UserAnswers(MoveCasesCacheKey)

  private def controller(permission: Set[Permission]) =
    new MoveCasesController(
      new RequestActionsWithPermissions(
        playBodyParsers,
        permission,
        addViewCasePermission = false
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
      viewUser,
      userNotFound,
      resourceNotFound
    )(realAppConfig, executionContext)

  private def controllerWithData(permission: Set[Permission], userAnswers: UserAnswers = userAnswersMock) =
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
      viewUser,
      userNotFound,
      resourceNotFound
    )(realAppConfig, executionContext)

  "postMoveATaRCases" should {

    "return 200 OK and HTML content type on form error" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(newFakePOSTRequestWithCSRF()))
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
        await(controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(newFakePOSTRequestWithCSRF()))
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "1"))
    }

    "redirect to chooseUserOrTeam on valid form with cases only open" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )
      val atarForm = MoveCasesForm.moveCasesForm("atarCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(atarForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserOrTeam().url)

    }

    "redirect to chooseUserToMoveCases on valid form with referred cases present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.REFERRED)
            )
          )
        )
      val atarForm = MoveCasesForm.moveCasesForm("atarCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(atarForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(None).url)

    }

    "redirect to chooseUserToMoveCases on valid form with suspended cases present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.SUSPENDED)
            )
          )
        )
      val atarForm = MoveCasesForm.moveCasesForm("atarCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(atarForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(None).url)

    }

    "redirect to unauthorised on valid form with cases with different status than Open/Referred/Suspended" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.COMPLETED)
            )
          )
        )
      val atarForm = MoveCasesForm.moveCasesForm("atarCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(atarForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveATaRCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).postMoveATaRCases("1")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "postMoveLiabilityCases" should {

    "return 200 OK and HTML content type on form error" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.liabilityCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(newFakePOSTRequestWithCSRF()))
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
        await(controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(newFakePOSTRequestWithCSRF()))
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "1"))
    }

    "redirect to chooseUserOrTeam on valid form with cases only open" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )
      val liabForm = MoveCasesForm.moveCasesForm("liabilityCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(liabForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserOrTeam().url)

    }

    "redirect to chooseUserToMoveCases on valid form with referred cases present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.REFERRED)
            )
          )
        )
      val liabForm = MoveCasesForm.moveCasesForm("liabilityCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(liabForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(None).url)

    }

    "redirect to chooseUserToMoveCases on valid form with suspended cases present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.SUSPENDED)
            )
          )
        )
      val liabForm = MoveCasesForm.moveCasesForm("liabilityCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(liabForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(None).url)

    }

    "redirect to unauthorised on valid form with cases with different status than Open/Referred/Suspended" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.COMPLETED)
            )
          )
        )
      val liabForm = MoveCasesForm.moveCasesForm("liabilityCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(liabForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveLiabCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.liabilityCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).postMoveLiabCases("1")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "postMoveCorrespondenceCases" should {

    "return 200 OK and HTML content type on form error" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.correspondenceCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(newFakePOSTRequestWithCSRF()))
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
        await(controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(newFakePOSTRequestWithCSRF()))
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "1"))
    }

    "redirect to chooseUserOrTeam on valid form with cases only open" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )
      val corrForm = MoveCasesForm.moveCasesForm("corrCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(corrForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserOrTeam().url)

    }

    "redirect to chooseUserToMoveCases on valid form with referred cases present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.REFERRED)
            )
          )
        )
      val corrForm = MoveCasesForm.moveCasesForm("corrCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(corrForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(None).url)

    }

    "redirect to chooseUserToMoveCases on valid form with suspended cases present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.SUSPENDED)
            )
          )
        )
      val corrForm = MoveCasesForm.moveCasesForm("corrCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(corrForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(None).url)

    }

    "redirect to unauthorised on valid form with cases with different status than Open/Referred/Suspended" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.COMPLETED)
            )
          )
        )
      val corrForm = MoveCasesForm.moveCasesForm("corrCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(corrForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveCorrCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.correspondenceCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).postMoveCorrCases("1")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "postMoveMiscCases" should {

    "return 200 OK and HTML content type on form error" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.miscellaneousCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)))
          .postMoveMiscCases("1")(newFakePOSTRequestWithCSRF())
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
        await(controller(Set(Permission.MANAGE_USERS)).postMoveMiscCases("1")(newFakePOSTRequestWithCSRF()))
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "1"))
    }

    "redirect to chooseUserOrTeam on valid form with cases only open" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )
      val miscForm = MoveCasesForm.moveCasesForm("miscCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(miscForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveMiscCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserOrTeam().url)

    }

    "redirect to chooseUserToMoveCases on valid form with referred cases present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.REFERRED)
            )
          )
        )
      val miscForm = MoveCasesForm.moveCasesForm("miscCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(miscForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveMiscCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(None).url)

    }

    "redirect to chooseUserToMoveCases on valid form with suspended cases present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.SUSPENDED)
            )
          )
        )
      val miscForm = MoveCasesForm.moveCasesForm("miscCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(miscForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveMiscCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(None).url)

    }

    "redirect to unauthorised on valid form with cases with different status than Open/Referred/Suspended" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(
              Cases.btiCaseExample
                .copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.COMPLETED)
            )
          )
        )
      val miscForm = MoveCasesForm.moveCasesForm("miscCases").fill(Set("100"))

      val fakeReqWithCases = newFakePOSTRequestWithCSRF(miscForm.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS)).postMoveMiscCases("1")(fakeReqWithCases)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.miscellaneousCaseExample)))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set()).postMoveMiscCases("1")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "chooseUserOrTeam" should {

    "return 200 OK and HTML content type" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS), userAnswersMock.set(ChosenCases, Set("100")))
            .chooseUserOrTeam()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return 200 OK and HTML content type when set is empty" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS))
            .chooseUserOrTeam()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result =
        await(controllerWithData(Set(), userAnswersMock.set(ChosenCases, Set("100"))).chooseUserOrTeam()(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))

      val result = await(controller(Set(Permission.MANAGE_USERS)).chooseUserOrTeam()(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "chooseUserToMoveCases" should {

    "return 200 OK and HTML content type" in {

      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2")))))

      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS), userAnswersMock.set(ChosenCases, Set("100")))
            .chooseUserToMoveCases()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return 200 OK and HTML content type for empty set" in {

      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1"))))

      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS))
            .chooseUserToMoveCases()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return 200 OK and HTML content type for teamID present" in {

      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1"))))

      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS), userAnswersMock.set(ChosenCases, Set("100")))
            .chooseUserToMoveCases(Some("2"))(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1"))))

      val result =
        await(
          controllerWithData(Set(), userAnswersMock.set(ChosenCases, Set("100"))).chooseUserToMoveCases()(fakeRequest)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1"))))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).chooseUserToMoveCases()(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "chooseOneOfUsersTeams" should {

    "return 200 OK and HTML content type" in {
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))
      given(queueService.getQueuesById(any[Seq[String]])).willReturn(Seq(Some(Queues.elm)))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(ChosenUserPID, "1")
          ).chooseOneOfUsersTeams()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return 200 OK and HTML content type for empty set" in {
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1")))
      given(queueService.getQueuesById(any[Seq[String]])).willReturn(Seq(Some(Queues.elm)))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(ChosenUserPID, "1")
          ).chooseOneOfUsersTeams()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return Not Found when user is not found" in {
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)
      given(queueService.getQueuesById(any[Seq[String]])).willReturn(Seq(Some(Queues.elm)))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(ChosenUserPID, "1")
          ).chooseOneOfUsersTeams()(newFakeGETRequestWithCSRF())
        )
      status(result) shouldBe Status.NOT_FOUND

    }

    "return Unauthorised when pid was not present" in {
      given(queueService.getQueuesById(any[Seq[String]])).willReturn(Seq(Some(Queues.elm)))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100"))
          ).chooseOneOfUsersTeams()(newFakeGETRequestWithCSRF())
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no permissions" in {
      val result =
        await(
          controllerWithData(Set(), userAnswersMock.set(ChosenCases, Set("100"))).chooseOneOfUsersTeams()(fakeRequest)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      val result =
        await(controller(Set(Permission.MANAGE_USERS)).chooseOneOfUsersTeams()(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "chooseUserFromAnotherTeam" should {

    "return 200 OK and HTML content type" in {

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(ChosenUserPID, "1")
          ).chooseUserFromAnotherTeam()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)

      val result =
        await(
          controllerWithData(Set(), userAnswersMock.set(ChosenCases, Set("100")))
            .chooseUserFromAnotherTeam()(fakeRequest)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).chooseUserFromAnotherTeam()(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "chooseTeamToMoveCases" should {

    "return 200 OK and HTML content type" in {

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(ChosenUserPID, "1")
          ).chooseTeamToMoveCases()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return 200 OK and HTML content type for empty set" in {

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)

      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS)).chooseTeamToMoveCases()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)

      val result =
        await(
          controllerWithData(Set(), userAnswersMock.set(ChosenCases, Set("100")))
            .chooseTeamToMoveCases()(fakeRequest)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).chooseTeamToMoveCases()(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "casesMovedToTeamDone" should {

    "return 200 OK and HTML content type" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))
      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "2")
              .set(ChosenTeam, "2")
          ).casesMovedToTeamDone()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return Not Found when user not found" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)
      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "2")
              .set(ChosenTeam, "2")
          ).casesMovedToTeamDone()(newFakeGETRequestWithCSRF())
        )
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "2"))

    }

    "return Not Found when team not found" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))
      given(queueService.getOneById(any[String])).willReturn(None)

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "ref")
              .set(ChosenTeam, "2")
          ).casesMovedToTeamDone()(newFakeGETRequestWithCSRF())
        )
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.resource-not-found.title", "Queue 2"))

    }

    "return Unauthorised for missing original user pid" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))
      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(ChosenTeam, "2")
          ).casesMovedToTeamDone()(newFakeGETRequestWithCSRF())
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return Unauthorised for missing teamID" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")))
      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "2")
          ).casesMovedToTeamDone()(newFakeGETRequestWithCSRF())
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no permissions" in {
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)
      given(queueService.getQueuesById(any[Seq[String]])).willReturn(Seq(Some(Queues.elm)))

      val result =
        await(
          controllerWithData(
            Set(),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "ref")
              .set(ChosenTeam, "2")
          ).casesMovedToTeamDone()(fakeRequest)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None)
      given(queueService.getQueuesById(any[Seq[String]])).willReturn(Seq(Some(Queues.elm)))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).casesMovedToTeamDone()(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "casesMovedToUserDone" should {

    "return 200 OK and HTML content type" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")), Some(Operator("2")))

      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "2")
              .set(ChosenUserPID, "1")
              .set(ChosenTeam, "2")
          ).casesMovedToUserDone()(newFakeGETRequestWithCSRF())
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return Not Found when original user not found" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(None, Some(Operator("1")))

      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "2")
              .set(ChosenUserPID, "1")
              .set(ChosenTeam, "2")
          ).casesMovedToUserDone()(newFakeGETRequestWithCSRF())
        )
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "2"))

    }

    "return Not Found when chosen user not found" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")), None)

      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "2")
              .set(ChosenUserPID, "1")
              .set(ChosenTeam, "2")
          ).casesMovedToUserDone()(newFakeGETRequestWithCSRF())
        )
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "1"))

    }

    "return Not Found when team not found" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")), Some(Operator("2")))

      given(queueService.getOneById(any[String])).willReturn(None)

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "ref")
              .set(ChosenUserPID, "1")
              .set(ChosenTeam, "2")
          ).casesMovedToUserDone()(newFakeGETRequestWithCSRF())
        )
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.resource-not-found.title", "Queue 2"))

    }

    "return Unauthorised for missing original user pid" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")), Some(Operator("2")))

      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(ChosenUserPID, "1")
              .set(ChosenTeam, "2")
          ).casesMovedToUserDone()(newFakeGETRequestWithCSRF())
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return Unauthorised for missing chosen user pid" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")), Some(Operator("2")))

      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "2")
              .set(ChosenTeam, "2")
          ).casesMovedToUserDone()(newFakeGETRequestWithCSRF())
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return Unauthorised for missing teamID" in {

      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")), Some(Operator("2")))

      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(ChosenUserPID, "1")
              .set(OriginalUserPID, "2")
          ).casesMovedToUserDone()(newFakeGETRequestWithCSRF())
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no permissions" in {
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")), Some(Operator("2")))
      given(queueService.getQueuesById(any[Seq[String]])).willReturn(Seq(Some(Queues.elm)))

      val result =
        await(
          controllerWithData(
            Set(),
            userAnswersMock
              .set(ChosenCases, Set("100"))
              .set(OriginalUserPID, "ref")
              .set(ChosenUserPID, "1")
              .set(ChosenTeam, "2")
          ).casesMovedToUserDone()(fakeRequest)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(userService.getUser(any[String])(any[HeaderCarrier])).willReturn(Some(Operator("1")), Some(Operator("2")))
      given(queueService.getQueuesById(any[Seq[String]])).willReturn(Seq(Some(Queues.elm)))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).casesMovedToUserDone()(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "postTeamOrUserChoice" should {

    "return 200 OK and HTML content type on form error" in {
      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS), userAnswersMock.set(ChosenCases, Set("100")))
            .postTeamOrUserChoice()(newFakePOSTRequestWithCSRF())
        )
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("move_cases.error.empty.teamOrUser"))

    }

    "redirect to chooseUserToMoveCases on valid form with User option" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      val form    = TeamOrUserForm.form.fill(TeamOrUser.USER)
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS), userAnswersMock.set(ChosenCases, Set("100")))
            .postTeamOrUserChoice()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases().url)

    }

    "redirect to chooseTeamToMoveCases on valid form with Team selected" in {
      val form    = TeamOrUserForm.form.fill(TeamOrUser.TEAM)
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(Set(Permission.MANAGE_USERS), userAnswersMock.set(ChosenCases, Set("100")))
            .postTeamOrUserChoice()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseTeamToMoveCases().url)

    }

    "return unauthorised with no permissions" in {
      val form    = TeamOrUserForm.form.fill(TeamOrUser.TEAM)
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(Set(), userAnswersMock.set(ChosenCases, Set("100")))
            .postTeamOrUserChoice()(fakeReq)
        )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      val form    = TeamOrUserForm.form.fill(TeamOrUser.TEAM)
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS))
            .postTeamOrUserChoice()(fakeReq)
        )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "postUserChoice" should {

    "return 200 OK and HTML content type on form error" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(assignee = Some(Operator("1")), queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2")))))
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))

      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postUserChoice(None)(newFakePOSTRequestWithCSRF())
        )
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("error.empty.moveCases.userToMove"))

    }

    "redirect to casesMovedToUserDone on valid form with User selected" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(assignee = Some(Operator("1")), queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2")))))
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))

      val form    = UserToMoveCaseForm.form.fill("1")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postUserChoice()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.casesMovedToUserDone().url)

    }

    "redirect to Not Found on valid form when user with posted pid is not present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(assignee = Some(Operator("1")), queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2")))))
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(None)

      val form    = UserToMoveCaseForm.form.fill("1")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postUserChoice()(fakeReq)
        )
      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("errors.user-not-found.message", "1"))

    }

    "redirect to chooseOneOfUsersTeams on valid form with User with more than 1 team selected" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(assignee = Some(Operator("1")), queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2", "3")))))
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2", "3"))))

      val form    = UserToMoveCaseForm.form.fill("1")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postUserChoice()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseOneOfUsersTeams().url)

    }

    "redirect to casesMovedToUserDone on valid form with User selected and teamId present" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(assignee = Some(Operator("1")), queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2")))))
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))

      val form    = UserToMoveCaseForm.form.fill("1")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postUserChoice(Some("2"))(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.casesMovedToUserDone().url)

    }

    "redirect to chooseUserFromAnotherTeam on valid form with OTHER selected" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(assignee = Some(Operator("1")), queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2")))))
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))

      val form    = UserToMoveCaseForm.form.fill("OTHER")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postUserChoice()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserFromAnotherTeam().url)

    }

    "return unauthorised with no permissions" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(assignee = Some(Operator("1")), queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2")))))
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))

      val form    = UserToMoveCaseForm.form.fill("1")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(Set(), userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1"))
            .postUserChoice()(fakeReq)
        )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(assignee = Some(Operator("1")), reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(assignee = Some(Operator("1")), queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getAllUsers(any[Seq[Role]], any[String], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Operator("1").copy(memberOfTeams = Seq("1", "2")))))
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))

      val form    = UserToMoveCaseForm.form.fill("1")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controller(Set(Permission.MANAGE_USERS))
            .postUserChoice()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "postChooseUserFromAnotherTeam" should {

    "return 200 OK and HTML content type on form error" in {
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postChooseUserFromAnotherTeam()(newFakePOSTRequestWithCSRF())
        )
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("error.empty.moveCases.teamToMove"))

    }

    "redirect to casesMovedToUserDone on valid form with User selected" in {
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postChooseUserFromAnotherTeam()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.chooseUserToMoveCases(Some("2")).url)

    }

    "return unauthorised with no permissions" in {
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postChooseUserFromAnotherTeam()(fakeReq)
        )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controller(
            Set(Permission.MANAGE_USERS)
          ).postChooseUserFromAnotherTeam()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "postTeamChoice" should {

    "return 200 OK and HTML content type on form error" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postTeamChoice()(newFakePOSTRequestWithCSRF())
        )
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("error.empty.moveCases.teamToMove"))

    }

    "redirect to casesMovedToTeamDone” on valid form with team selected" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postTeamChoice()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.casesMovedToTeamDone().url)

    }

    "return unauthorised with no permissions" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1")
          ).postTeamChoice()(fakeReq)
        )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controller(
            Set(Permission.MANAGE_USERS)
          ).postTeamChoice()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

  "postChooseOneOfUsersTeams" should {

    "return 200 OK and HTML content type on form error" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))
      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1").set(ChosenUserPID, "2")
          ).postChooseOneOfUsersTeams()(newFakePOSTRequestWithCSRF())
        )
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("error.empty.moveCases.teamToMove"))

    }

    "redirect to casesMovedToUserDone” on valid form with team selected" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))

      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(Permission.MANAGE_USERS),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1").set(ChosenUserPID, "2")
          ).postChooseOneOfUsersTeams()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MoveCasesController.casesMovedToUserDone().url)

    }

    "return unauthorised with no permissions" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )
      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))

      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))

      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controllerWithData(
            Set(),
            userAnswersMock.set(ChosenCases, Set("100")).set(OriginalUserPID, "1").set(ChosenUserPID, "2")
          ).postChooseOneOfUsersTeams()(fakeReq)
        )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

    "return unauthorised with no data" in {
      given(dataCacheConnector.save(any[CacheMap])).willReturn(userAnswersMock.set(ChosenCases, Set("100")).cacheMap)
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Paged(
            Seq(Cases.btiCaseExample.copy(reference = "100", status = CaseStatus.OPEN))
          )
        )

      given(casesService.updateCase(any[Case], any[Case], any[Operator])(any[HeaderCarrier])).willReturn(
        Cases.btiCaseExample
          .copy(queueId = Some("2"), reference = "100", status = CaseStatus.OPEN)
      )

      given(userService.getUser(any[String])(any[HeaderCarrier]))
        .willReturn(Some(Operator("1").copy(memberOfTeams = Seq("1", "2"))))
      given(queueService.getOneById(any[String])).willReturn(Some(Queues.elm))
      given(queueService.getNonGateway).willReturn(Queues.allDynamicQueues)
      val form    = TeamToMoveCaseForm.form.fill("2")
      val fakeReq = newFakePOSTRequestWithCSRF(form.data)
      val result =
        await(
          controller(
            Set(Permission.MANAGE_USERS)
          ).postChooseOneOfUsersTeams()(fakeReq)
        )
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)

    }

  }

}
