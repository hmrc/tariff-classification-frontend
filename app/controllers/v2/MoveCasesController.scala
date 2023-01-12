/*
 * Copyright 2023 HM Revenue & Customs
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
import models.forms.v2._
import models.request.{AuthenticatedDataRequest, AuthenticatedRequest}
import models.viewmodels._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, QueuesService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.partials.users._
import views.html.{resource_not_found, user_not_found}

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class MoveCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  userService: UserService,
  queueService: QueuesService,
  dataCacheConnector: DataCacheConnector,
  mcc: MessagesControllerComponents,
  val teamOrUserPage: move_cases_team_or_user,
  val chooseTeamPage: move_cases_choose_team,
  val chooseTeamToChooseUsersFromPage: move_cases_choose_user_team,
  val chooseUserPage: move_cases_choose_user,
  val chooseUserTeamPage: move_cases_choose_one_from_user_teams,
  val doneMoveCasesPage: done_move_cases,
  val viewUser: view_user,
  val user_not_found: user_not_found,
  val resource_not_found: resource_not_found
)(
  implicit val appConfig: AppConfig,
  implicit val ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private val moveATaRCasesForm = MoveCasesForm.moveCasesForm("atarCases")
  private val moveLiabCasesForm = MoveCasesForm.moveCasesForm("liabilityCases")
  private val moveCorrCasesForm = MoveCasesForm.moveCasesForm("corrCases")
  private val moveMiscCasesForm = MoveCasesForm.moveCasesForm("miscCases")
  private val MoveCasesCacheKey = "move_cases"
  private val ChosenCases       = "chosen_cases"
  private val ChosenTeam        = "chosen_team"
  private val ChosenUserPID     = "chosen_user_pid"
  private val OriginalUserPID   = "original_user_pid"
  private val teamOrUserForm    = TeamOrUserForm.form
  private val chooseTeamForm    = TeamToMoveCaseForm.form
  private val chooseUserForm    = UserToMoveCaseForm.form

  def postMoveATaRCases(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      val userAnswers = UserAnswers(MoveCasesCacheKey)
      moveATaRCasesForm
        .bindFromRequest()
        .fold(
          errors => renderViewUserPageWithErrors(pid, errors, moveLiabCasesForm, moveCorrCasesForm, moveMiscCasesForm),
          casesIds => redirectAfterPostingCaseRefs(casesIds, pid, userAnswers)
        )
    }

  def postMoveLiabCases(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      val userAnswers = UserAnswers(MoveCasesCacheKey)
      moveLiabCasesForm
        .bindFromRequest()
        .fold(
          errors => renderViewUserPageWithErrors(pid, moveATaRCasesForm, errors, moveCorrCasesForm, moveMiscCasesForm),
          casesIds => redirectAfterPostingCaseRefs(casesIds, pid, userAnswers)
        )
    }

  def postMoveCorrCases(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      val userAnswers = UserAnswers(MoveCasesCacheKey)
      moveCorrCasesForm
        .bindFromRequest()
        .fold(
          errors => renderViewUserPageWithErrors(pid, moveATaRCasesForm, moveLiabCasesForm, errors, moveMiscCasesForm),
          casesIds => redirectAfterPostingCaseRefs(casesIds, pid, userAnswers)
        )
    }

  def postMoveMiscCases(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      val userAnswers = UserAnswers(MoveCasesCacheKey)
      moveMiscCasesForm
        .bindFromRequest()
        .fold(
          errors => renderViewUserPageWithErrors(pid, moveATaRCasesForm, moveLiabCasesForm, moveCorrCasesForm, errors),
          casesIds => redirectAfterPostingCaseRefs(casesIds, pid, userAnswers)
        )
    }

  def chooseUserOrTeam(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )) { implicit request =>
      val caseNumber = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty).size
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
      val teams      = teamId.map(Seq(_)).getOrElse(request.operator.memberOfTeams.filterNot(_ == Queues.gateway.id))
      val usersOfManagedTeams = teams
        .map(team =>
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
      val caseRefs = request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty)
      val teams    = teamId.map(Seq(_)).getOrElse(request.operator.memberOfTeams.filterNot(_ == Queues.gateway.id))
      val usersOfManagedTeams = teams
        .map(team =>
          for {
            usersOfManagedTeam <- userService
                                   .getAllUsers(
                                     Seq(Role.CLASSIFICATION_OFFICER, Role.CLASSIFICATION_MANAGER),
                                     team,
                                     NoPagination()
                                   )
          } yield usersOfManagedTeam.results
        )

      chooseUserForm
        .bindFromRequest()
        .fold(
          errors =>
            Future
              .sequence(usersOfManagedTeams)
              .map(_.flatten)
              .flatMap(users => successful(Ok(chooseUserPage(caseRefs.size, users.distinct, errors, teamId)))),
          userPid =>
            userPid match {
              case "OTHER" => successful(Redirect(routes.MoveCasesController.chooseUserFromAnotherTeam()))
              case _       => moveToUser(userPid, caseRefs)
            }
        )
    }

  def chooseOneOfUsersTeams(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async(implicit request =>
      request.userAnswers
        .get[String](ChosenUserPID)
        .map(pid =>
          for {
            user <- userService.getUser(pid)
            teams <- user
                      .map(u => queueService.getQueuesById(u.memberOfTeams.filterNot(_ == Queues.gateway.id)))
                      .getOrElse(Future.successful(Seq()))
          } yield {
            user
              .map(u =>
                Ok(
                  chooseUserTeamPage(
                    u.safeName,
                    u.memberOfTeams.size,
                    request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set()).size,
                    chooseTeamForm,
                    teams.flatten
                  )
                )
              )
              .getOrElse(NotFound(user_not_found(pid)))
          }
        )
        .getOrElse(successful(Redirect(controllers.routes.SecurityController.unauthorized())))
    )

  def postChooseOneOfUsersTeams(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async(implicit request =>
      chooseTeamForm
        .bindFromRequest()
        .fold(
          errors =>
            request.userAnswers
              .get[String](ChosenUserPID)
              .map(pid =>
                for {
                  user <- userService.getUser(pid)
                  teams <- user
                            .map(u => queueService.getQueuesById(u.memberOfTeams.filterNot(_ == Queues.gateway.id)))
                            .getOrElse(Future.successful(Seq()))
                } yield {
                  user
                    .map(u =>
                      Ok(
                        chooseUserTeamPage(
                          u.safeName,
                          u.memberOfTeams.size,
                          request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set()).size,
                          errors,
                          teams.flatten
                        )
                      )
                    )
                    .getOrElse(NotFound(user_not_found("")))
                }
              )
              .getOrElse(successful(NotFound(user_not_found("")))),
          team => {
            request.userAnswers
              .get[String](ChosenUserPID)
              .map(pid =>
                casesService.updateCases(
                  request.userAnswers.get[Set[String]](ChosenCases).getOrElse(Set.empty),
                  Some(Operator(pid)),
                  team,
                  request.userAnswers.get[String](OriginalUserPID).get,
                  request.operator.id
                )
              )
            for {
              _ <- dataCacheConnector.save(request.userAnswers.set(ChosenTeam, team).cacheMap)
            } yield Redirect(routes.MoveCasesController.casesMovedToUserDone())
          }
        )
    )

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
        queues <- queueService.getNonGateway
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
              queues <- queueService.getNonGateway
            } yield Ok(chooseTeamPage(caseNumber, errors, queues)),
          team => {
            casesService.updateCases(
              caseRefs,
              None,
              team,
              request.userAnswers.get[String](OriginalUserPID).get,
              request.operator.id
            )
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
      request.userAnswers
        .get[String](OriginalUserPID)
        .map { originalPID =>
          request.userAnswers
            .get[String](ChosenTeam)
            .map(teamID =>
              for {
                user <- userService.getUser(originalPID)
                team <- queueService.getOneById(teamID)
              } yield (user, team) match {
                case (Some(u), Some(t)) => Ok(doneMoveCasesPage(u.safeName, t.slug.toUpperCase))
                case (None, _)          => NotFound(user_not_found(originalPID))
                case (_, None)          => NotFound(resource_not_found(s"Queue " + teamID))
              }
            )
            .getOrElse(successful(Redirect(controllers.routes.SecurityController.unauthorized())))
        }
        .getOrElse(successful(Redirect(controllers.routes.SecurityController.unauthorized())))
    }

  def casesMovedToUserDone(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS) andThen verify.requireData(
      MoveCasesCacheKey
    )).async { implicit request =>
      request.userAnswers
        .get[String](OriginalUserPID)
        .map(originalPID =>
          request.userAnswers
            .get[String](ChosenUserPID)
            .map(chosenPID =>
              request.userAnswers
                .get[String](ChosenTeam)
                .map(teamID =>
                  for {
                    originalUser <- userService.getUser(originalPID)
                    newUser      <- userService.getUser(chosenPID)
                    team         <- queueService.getOneById(teamID)
                  } yield (originalUser, newUser, team) match {
                    case (Some(ou), Some(nu), Some(t)) =>
                      Ok(doneMoveCasesPage(ou.safeName, t.slug.toUpperCase, Some(nu.safeName)))
                    case (None, _, _) => NotFound(user_not_found(originalPID))
                    case (_, None, _) => NotFound(user_not_found(chosenPID))
                    case (_, _, None) => NotFound(resource_not_found(s"Queue " + teamID))
                  }
                )
                .getOrElse(successful(Redirect(controllers.routes.SecurityController.unauthorized())))
            )
            .getOrElse(successful(Redirect(controllers.routes.SecurityController.unauthorized())))
        )
        .getOrElse(successful(Redirect(controllers.routes.SecurityController.unauthorized())))

    }

  private def findChosenCasesInAssignedCases(assignedCases: Seq[Case], chosenCaseRefs: Set[String]) =
    chosenCaseRefs.map(ref => assignedCases.find(c => c.reference == ref)).flatten

  private def redirectBasedOnCaseStatus(chosenCases: Set[Case]) =
    if (chosenCases.exists(c =>
          c.status != CaseStatus.OPEN && c.status != CaseStatus.REFERRED && c.status != CaseStatus.SUSPENDED
        )) {
      Redirect(controllers.routes.SecurityController.unauthorized())
    } else if (chosenCases.exists(c => c.status == CaseStatus.REFERRED || c.status == CaseStatus.SUSPENDED)) {
      Redirect(routes.MoveCasesController.chooseUserToMoveCases())
    } else {
      Redirect(routes.MoveCasesController.chooseUserOrTeam())
    }

  private def redirectAfterPostingCaseRefs(caseRefs: Set[String], pid: String, userAnswers: UserAnswers)(
    implicit hc: HeaderCarrier
  ) = {
    val userAnswersWithUserPID = userAnswers.set(OriginalUserPID, pid)
    val userAnswersWithCases   = userAnswersWithUserPID.set(ChosenCases, caseRefs)
    for {
      _     <- dataCacheConnector.save(userAnswersWithCases.cacheMap)
      cases <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
    } yield redirectBasedOnCaseStatus(findChosenCasesInAssignedCases(cases.results, caseRefs))
  }

  private def renderViewUserPageWithErrors(
    pid: String,
    atarForm: Form[Set[String]],
    liabForm: Form[Set[String]],
    corrForm: Form[Set[String]],
    miscForm: Form[Set[String]]
  )(implicit hc: HeaderCarrier, request: AuthenticatedRequest[_]) =
    for {
      userTab <- userService.getUser(pid)
      cases   <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
      userCaseTabs = ApplicationsTab.casesByTypes(cases.results)
    } yield userTab
      .map(user => Ok(viewUser(user, userCaseTabs, atarForm, liabForm, corrForm, miscForm)))
      .getOrElse(NotFound(user_not_found(pid)))

  private def moveToUser(
    pid: String,
    caseRefs: Set[String]
  )(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]) =
    userService.getUser(pid).flatMap {
      case Some(u) => {
        val userAnswersWithNewUser = request.userAnswers.set(ChosenUserPID, pid)
        if (u.memberOfTeams.filterNot(_ == Queues.gateway.id).size == 1) {
          casesService.updateCases(
            caseRefs,
            Some(u),
            u.memberOfTeams.head,
            request.userAnswers.get[String](OriginalUserPID).get,
            request.operator.id
          )
          for {
            _ <- dataCacheConnector.save(
                  userAnswersWithNewUser
                    .set(ChosenTeam, u.memberOfTeams.filterNot(_ == Queues.gateway.id).head)
                    .cacheMap
                )
          } yield Redirect(routes.MoveCasesController.casesMovedToUserDone())
        } else {
          for {
            _ <- dataCacheConnector.save(userAnswersWithNewUser.cacheMap)
          } yield Redirect(routes.MoveCasesController.chooseOneOfUsersTeams())
        }
      }
      case _ => successful(NotFound(user_not_found(pid)))
    }

}
