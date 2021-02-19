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

import com.google.inject.Inject
import config.AppConfig
import connector.DataCacheConnector
import controllers.RequestActions
import models._
import models.forms.v2.{MoveCasesForm, TeamOrUser, TeamOrUserForm, TeamToMoveCaseForm}
import models.request.AuthenticatedRequest
import models.viewmodels.{ManagerToolsUsersTab, SubNavigationTab, _}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, QueuesService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.HeaderNames

import scala.concurrent.Future.successful

class MoveCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  userService: UserService,
  queueService: QueuesService,
  dataCacheConnector: DataCacheConnector,
  mcc: MessagesControllerComponents,
  val teamOrUserPage: views.html.partials.users.move_cases_team_or_user,
  val chooseTeamPage: views.html.partials.users.move_cases_choose_team,
  val doneMoveCasesPage: views.html.partials.users.done_move_cases
)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private val moveATaRCasesForm = MoveCasesForm.moveCasesForm
  private val MoveCasesCacheKey = "move_cases"
  private val ChosenCases       = "chosen_cases"
  private val ChosenTeam        = "chosen_team"
  private val teamOrUserForm    = TeamOrUserForm.form
  private val chooseTeamForm    = TeamToMoveCaseForm.form

  def chooseUserOrTeam(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )) { implicit request =>
      val caseNumber = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty).size
      Ok(teamOrUserPage(caseNumber, teamOrUserForm))
    }

  def postMoveCases(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      val userAnswers = UserAnswers(MoveCasesCacheKey)
      moveATaRCasesForm
        .bindFromRequest()
        .fold(
          // There is no form validation so this should not be possible
          errors => Future.successful(Redirect(request.headers(HeaderNames.REFERER))),
          cases =>
            for {
              _ <- dataCacheConnector.save(userAnswers.set(ChosenCases, cases).cacheMap)
            } yield Redirect(routes.MoveCasesController.chooseUserOrTeam())
        )
    }

  def postTeamOrUserChoice(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )) { implicit request =>
      val caseNumber = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty).size
      teamOrUserForm
        .bindFromRequest()
        .fold(
          errors => Ok(teamOrUserPage(caseNumber, errors)),
          choice =>
            choice match {
              case TeamOrUser.TEAM => Redirect(routes.MoveCasesController.chooseTeamToMoveCases())
              case _               => ???
            }
        )

    }

  def chooseTeamToMoveCases(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )) { implicit request =>
      val caseNumber = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty).size
      Ok(chooseTeamPage(caseNumber, chooseTeamForm))
    }

  def postTeamChoice(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      val caseNumber  = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty).size
      val caseRefs    = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty)
      val userAnswers = UserAnswers(MoveCasesCacheKey)

      chooseTeamForm
        .bindFromRequest()
        .fold(
          errors => Future.successful(Ok(chooseTeamPage(caseNumber, errors))),
          team => {
            val updatedCases =
              caseRefs.map(ref =>
                for {
                  updatedCase <- casesService.getOne(ref) flatMap {
                                  case Some(c) => casesService.updateCase(c.copy(assignee = None, queueId = Some(team)))
                                }

                } yield updatedCase
              )
            for {
              _ <- dataCacheConnector.save(userAnswers.set(ChosenTeam, team).cacheMap)
            } yield Redirect(routes.MoveCasesController.casesMovedToTeamDone())
          }
        )
    }

  def casesMovedToTeamDone(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      request.userAnswers
        .get[String](ChosenTeam)
        .map(teamId =>
          queueService.getOneById(teamId) flatMap {
            case Some(team) => successful(Ok(doneMoveCasesPage(team.slug.toUpperCase)))
            case None       => successful(Ok(views.html.resource_not_found(s"Case Queue")))
          }
        )
        .getOrElse(successful(Ok(views.html.resource_not_found(s"Case Queue"))))
    }

}
