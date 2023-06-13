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

import akka.stream.Materializer
import config.AppConfig
import controllers.RequestActions
import models._
import models.forms.v2.{MoveCasesForm, RemoveUserForm, UserEditTeamForm}
import models.request.AuthenticatedRequest
import models.viewmodels._
import models.viewmodels.managementtools.UsersTabViewModel
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.managementtools.manage_users_view
import views.html.partials.users._
import views.html.user_not_found

import javax.inject.Inject
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class ManageUserController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  userService: UserService,
  mcc: MessagesControllerComponents,
  val viewUser: view_user,
  val user_team_edit: user_team_edit,
  val manageUsersView: manage_users_view,
  val cannotDeleteUser: cannot_delete_user,
  val confirmDeleteUser: confirm_delete_user,
  val doneDeleteUserPage: done_delete_user,
  val user_not_found: user_not_found
)(
  implicit
  val appConfig: AppConfig,
  mat: Materializer
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  implicit val ec: ExecutionContext = mat.executionContext

  private val userEditTeamForm                   = UserEditTeamForm.editTeamsForm
  private lazy val removeUserForm: Form[Boolean] = RemoveUserForm.form
  private val moveATaRCasesForm                  = MoveCasesForm.moveCasesForm("atarCases")
  private val moveLiabCasesForm                  = MoveCasesForm.moveCasesForm("liabilityCases")
  private val moveCorrCasesForm                  = MoveCasesForm.moveCasesForm("corrCases")
  private val moveMiscCasesForm                  = MoveCasesForm.moveCasesForm("miscCases")

  val assignedCases                   = "some"
  private val AssignedCasesPagination = SearchPagination(pageSize = 1000)

  def displayManageUsers(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      userService.getUser(request.operator.id).flatMap {
        case Some(manager) =>
          val managerQueues = manager.memberOfTeams.flatMap(id => Queues.queueById(id))

          for {
            allUsers <- userService
                         .getAllUsers(Seq(Role.CLASSIFICATION_OFFICER, Role.CLASSIFICATION_MANAGER), "", NoPagination())

            managerTeamsCases <- Paged
                                  .stream(AssignedCasesPagination) { pagination =>
                                    casesService
                                      .getCasesByAllQueues(managerQueues, pagination, assignee = assignedCases)
                                  }
                                  .runFold(List.empty[Case]) { case (cases, nextCase) => nextCase :: cases }

            usersWithCount = managerTeamsCases
              .groupBy(singleCase => singleCase.assignee.map(_.id))
              .collect { case (Some(pid), cases) => pid -> cases }

            usersTabViewModel = UsersTabViewModel.fromUsers(manager, allUsers)

          } yield Ok(manageUsersView(activeSubNav, usersTabViewModel, usersWithCount))
        case _ => Future(NotFound(user_not_found(request.operator.id)))
      }
    }

  def displayUserDetails(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userTab <- userService.getUser(pid)
          cases   <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          userCaseTabs = ApplicationsTab.casesByTypes(cases.results)
        } yield userTab
          .map(user =>
            Ok(viewUser(user, userCaseTabs, moveATaRCasesForm, moveLiabCasesForm, moveCorrCasesForm, moveMiscCasesForm))
          )
          .getOrElse(NotFound(user_not_found(pid)))
    }

  def deleteUser(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userCases <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          user      <- userService.getUser(pid)
        } yield {
          if (user.isDefined) {
            (user.get.id, userCases.results.nonEmpty) match {
              case (request.operator.id, _) => Redirect(controllers.routes.SecurityController.unauthorized())
              case (_, true)                => Ok(cannotDeleteUser(user.get))
              case (_, _)                   => Ok(confirmDeleteUser(user.get, removeUserForm))
            }
          } else {
            NotFound(user_not_found(pid))
          }

        }
    }

  def confirmRemoveUser(pid: String): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(
      Permission.MANAGE_USERS
    )).async(implicit request =>
      removeUserForm
        .bindFromRequest()
        .fold(
          errors =>
            for {
              user <- userService.getUser(pid)
            } yield user
              .map(u => Ok(confirmDeleteUser(u, errors)))
              .getOrElse(NotFound(user_not_found(pid))), {
            case true =>
              for {
                user <- userService.getUser(pid)
              } yield {
                user
                  .map { u =>
                    userService.markDeleted(u, request.operator)
                    Redirect(controllers.v2.routes.ManageUserController.doneDeleteUser(u.safeName))
                  }
                  .getOrElse(NotFound(user_not_found(pid)))
              }
            case _ =>
              successful(
                Redirect(controllers.v2.routes.ManageUserController.displayUserDetails(pid))
              )
          }
        )
    )

  def doneDeleteUser(userName: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)) {
      implicit request: AuthenticatedRequest[AnyContent] => Ok(doneDeleteUserPage(userName))
    }

  def editUserTeamDetails(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        userService
          .getUser(pid)
          .map {
            case Some(userDetails) =>
              Ok(user_team_edit(userDetails, userEditTeamForm.fill(userDetails.memberOfTeams.toSet)))
            case _ => NotFound(user_not_found(pid))
          }
    }

  def postEditUserTeams(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      userEditTeamForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(Ok(user_team_edit(Operator(pid), formWithErrors))),
          updatedMemberOfTeams =>
            userService
              .getUser(pid)
              .map {
                case Some(user) =>
                  userService.updateUser(user, user.copy(memberOfTeams = updatedMemberOfTeams.toSeq), request.operator)
                case _ => NotFound(user_not_found(pid))
              }
              .map(_ => Redirect(routes.ManageUserController.displayUserDetails(pid)))
        )
    }
}
