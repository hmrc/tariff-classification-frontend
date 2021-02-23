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
import models.forms.v2.{MoveCasesForm, TeamOrUser, TeamOrUserForm, TeamToMoveCaseForm, UserToMoveCaseForm}
import models.request.AuthenticatedRequest
import models.viewmodels.{ManagerToolsUsersTab, SubNavigationTab, _}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, QueuesService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.HeaderNames
import uk.gov.hmrc.http.HeaderCarrier

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
  val chooseTeamToChooseUsersFromPage: views.html.partials.users.move_cases_choose_user_team,
  val chooseUserPage: views.html.partials.users.move_cases_choose_user,
  val doneMoveCasesPage: views.html.partials.users.done_move_cases
)(
  implicit val appConfig: AppConfig,
  implicit val ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private val moveATaRCasesForm = MoveCasesForm.moveCasesForm
  private val MoveCasesCacheKey = "move_cases"
  private val ChosenCases       = "chosen_cases"
  private val ChosenTeam        = "chosen_team"
  private val ChosenUser        = "chosen_user"
  private val OriginalUser      = "original_user"
  private val teamOrUserForm    = TeamOrUserForm.form
  private val chooseTeamForm    = TeamToMoveCaseForm.form
  private val chooseUserForm    = UserToMoveCaseForm.form

  def postMoveCases(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      val userAnswers = UserAnswers(MoveCasesCacheKey)
      moveATaRCasesForm
        .bindFromRequest()
        .fold(
          // Add form validation
          errors => Future.successful(Redirect(request.headers(HeaderNames.REFERER))),
          cases => {
            val userName = for {
              originalUser <- userService.getUser(pid)
            } yield originalUser.map(_.safeName)
            userName.flatMap(uN =>
              uN.map { u =>
                  val userAnswersWithUserName = userAnswers.set(OriginalUser, u)
                  val userAnswersWithCases    = userAnswersWithUserName.set(ChosenCases, cases)
                  for {
                    _ <- dataCacheConnector.save(userAnswersWithCases.cacheMap)
                  } yield Redirect(routes.MoveCasesController.chooseUserOrTeam())
                }
                .getOrElse(successful(NotFound(views.html.user_not_found(""))))
            )
          }
        )
    }

  def chooseUserOrTeam(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )) { implicit request =>
      val caseNumber       = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty).size
      val originalUserName = request.userAnswers.get[String](OriginalUser).getOrElse("unknown")
      Ok(teamOrUserPage(caseNumber, teamOrUserForm))
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
              case _               => Redirect(routes.MoveCasesController.chooseUserToMoveCases())
            }
        )

    }

  def chooseUserToMoveCases(
    teamId: Option[String]         = None,
    activeSubNav: SubNavigationTab = ManagerToolsUsersTab
  ): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      val caseNumber = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty).size
      val teams      = teamId.map(Seq(_)).getOrElse(request.operator.memberOfTeams)
      val usersOfManagedTeams = teams.map(team =>
        for {
          usersOfManagedTeam <- userService.getAllUsers(
                                 Seq(Role.CLASSIFICATION_OFFICER, Role.CLASSIFICATION_MANAGER),
                                 team,
                                 NoPagination()
                               )
        } yield usersOfManagedTeam.results
      )
      Future
        .sequence(usersOfManagedTeams)
        .map(_.flatten)
        .flatMap(users => successful(Ok(chooseUserPage(caseNumber, users.distinct, chooseUserForm, teamId))))

    }

  def postUserChoice(
    teamId: Option[String]         = None,
    activeSubNav: SubNavigationTab = ManagerToolsUsersTab
  ): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      val caseRefs   = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty)
      val caseNumber = caseRefs.size
      val teams      = teamId.map(Seq(_)).getOrElse(request.operator.memberOfTeams)
      val usersOfManagedTeams = teams.map(team =>
        for {
          usersOfManagedTeam <- userService.getAllUsers(
                                 Seq(Role.CLASSIFICATION_OFFICER, Role.CLASSIFICATION_MANAGER),
                                 team,
                                 NoPagination()
                               )
        } yield usersOfManagedTeam.results
      )

      def moveToUser(pid: String, caseRefs: Set[String]) =
        for {
          user <- userService.getUser(pid)
        } yield {
          user
            .map(u =>
              if (u.memberOfTeams.size == 1) {
                val updatedCases           = updateCases(caseRefs, Some(u), u.memberOfTeams.head)
                val userAnswersWithNewUser = request.userAnswers.set(ChosenUser, u.safeName)
                for {
                  _ <- dataCacheConnector.save(userAnswersWithNewUser.set(ChosenTeam, u.memberOfTeams.head).cacheMap)
                } yield Redirect(routes.MoveCasesController.casesMovedToUserDone())
              } else {
                successful(NotFound(views.html.user_not_found(pid)))
              }
            )
            .getOrElse(successful(NotFound(views.html.user_not_found(pid))))
        }

      chooseUserForm
        .bindFromRequest()
        .fold(
          errors =>
            Future
              .sequence(usersOfManagedTeams)
              .map(_.flatten)
              .flatMap(users => successful(Ok(chooseUserPage(caseNumber, users.distinct, errors, teamId)))),
          userPid =>
            userPid match {
              case "OTHER" => successful(Redirect(routes.MoveCasesController.chooseUserFromAnotherTeam()))
              case _       => moveToUser(userPid, caseRefs).flatten
            }
        )
    }

  def chooseUserFromAnotherTeam(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async(implicit request =>
      for {
        nonGatewayQueues <- queueService.getNonGateway
      } yield Ok(chooseTeamToChooseUsersFromPage(chooseTeamForm, nonGatewayQueues))
    )

  def postChooseUserFromAnotherTeam(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      chooseTeamForm
        .bindFromRequest()
        .fold(
          errors =>
            for {
              nonGatewayQueues <- queueService.getNonGateway
            } yield Ok(chooseTeamToChooseUsersFromPage(errors, nonGatewayQueues)),
          team => successful(Redirect(routes.MoveCasesController.chooseUserToMoveCases(teamId = Some(team))))
        )
    }

  def chooseTeamToMoveCases(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      val caseNumber = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty).size
      for {
        queues <- queueService.getAll
      } yield Ok(chooseTeamPage(caseNumber, chooseTeamForm, queues))
    }

  def postTeamChoice(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      val caseRefs   = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty)
      val caseNumber = caseRefs.size

      chooseTeamForm
        .bindFromRequest()
        .fold(
          errors =>
            for {
              queues <- queueService.getAll
            } yield Ok(chooseTeamPage(caseNumber, errors, queues)),
          team => {
            //todo handle moving to gateway
            val updatedCases = updateCases(caseRefs, None, team)
            for {
              _ <- dataCacheConnector.save(request.userAnswers.set(ChosenTeam, team).cacheMap)
            } yield Redirect(routes.MoveCasesController.casesMovedToTeamDone())
          }
        )
    }

  def casesMovedToTeamDone(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      val originalUserName = request.userAnswers.get[String](OriginalUser).getOrElse("Unknown")
      request.userAnswers
        .get[String](ChosenTeam)
        .map(teamId =>
          queueService.getOneById(teamId) flatMap {
            case Some(team) => successful(Ok(doneMoveCasesPage(originalUserName, team.slug.toUpperCase)))
            case None       => successful(Ok(views.html.resource_not_found(s"Case Queue")))
          }
        )
        .getOrElse(successful(Ok(views.html.resource_not_found(s"Case Queue"))))
    }

  def casesMovedToUserDone(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      val originalUserName = request.userAnswers.get[String](OriginalUser)
      val newUserName      = request.userAnswers.get[String](ChosenUser)
      request.userAnswers
        .get[String](ChosenTeam)
        .map(teamId =>
          queueService.getOneById(teamId) flatMap {
            case Some(team) =>
              successful(Ok(doneMoveCasesPage(originalUserName.get, team.slug.toUpperCase, newUserName)))
            case None => successful(Ok(views.html.resource_not_found(s"Case Queue")))
          }
        )
        .getOrElse(successful(Ok(views.html.resource_not_found(s"Case Queue"))))
    }

  private def updateCases(refs: Set[String], user: Option[Operator], teamId: String)(implicit hc: HeaderCarrier) = {
    val updatedCases =
      refs.map(ref =>
        for {
          updatedCase <- casesService.getOne(ref)(hc) flatMap {
                          case Some(c) => casesService.updateCase(c.copy(assignee = user, queueId = Some(teamId)))
                        }
        } yield updatedCase
      )
    updatedCases
  }
}
