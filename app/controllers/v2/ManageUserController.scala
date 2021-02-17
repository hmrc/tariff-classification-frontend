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
import controllers.RequestActions
import models._
import models.forms.v2.UserEditTeamForm
import models.request.AuthenticatedRequest
import models.viewmodels.managementtools.UsersTabViewModel
import models.viewmodels.{ManagerToolsUsersTab, SubNavigationTab, _}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, EventsService, UserService}
import service.{CasesService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class ManageUserController @Inject()(
  verify: RequestActions,
  casesService: CasesService,
  eventsService: EventsService,
  userService: UserService,
  mcc: MessagesControllerComponents,
  val viewUser: views.html.partials.users.view_user,
  val user_team_edit: views.html.partials.users.user_team_edit,
  val manageUsersView: views.html.managementtools.manage_users_view
)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private val userEditTeamForm = UserEditTeamForm.editTeamsForm

  val Unassigned    = "unassigned"
  val assignedCases = "some"

  def displayManageUsers(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>

      userService.getUser(request.operator.id).flatMap {
        case Some(manager) => {
          val managerQueues = manager.memberOfTeams.flatMap(id => Queues.queueById(id))

          for {
            allUsers          <- userService.getAllUsers(Role.CLASSIFICATION_OFFICER, "", NoPagination())
            managerTeamsCases <- casesService.getCasesByAllQueues(managerQueues, NoPagination(), assignee = assignedCases)
            usersWithCount    = managerTeamsCases.results.toList
              .groupBy(singleCase => singleCase.assignee.map(_.id).getOrElse(Unassigned))
              .filterKeys(_ != Unassigned)
            usersTabViewModel = UsersTabViewModel.fromUsers(manager, allUsers)
          } yield Ok(manageUsersView(activeSubNav, usersTabViewModel, usersWithCount))
        }
        case _ => Future(NotFound(views.html.user_not_found("")))
      }
    }

  def displayUserDetails(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userTab <- userService.getUser(pid)
          cases <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          userCaseTabs = ApplicationsTab.casesByTypes(cases.results)
        } yield
          userTab
            .map(user => Ok(viewUser(user, userCaseTabs)))
            .getOrElse(NotFound(views.html.user_not_found(pid)))
    }


def editUserTeamDetails(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent]  =>
      userService
        .getUser(pid)
        .map {
          case Some(userDetails) =>  Ok(user_team_edit(userDetails, userEditTeamForm.fill(userDetails.memberOfTeams.toSet), activeSubNav))
          case _ => NotFound(views.html.user_not_found(pid))
        }
    }

  def postEditUserTeams(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      userEditTeamForm.bindFromRequest.fold(
        formWithErrors => Future.successful(Ok(user_team_edit(Operator(pid), formWithErrors, activeSubNav))),
        updatedMemberOfTeams =>
          userService
            .getUser(pid)
            .map(user =>
              userService.updateUser(user.get.copy(memberOfTeams = updatedMemberOfTeams.toSeq), request.operator)
            )
            .map(_ => Redirect(routes.ManageUserController.displayUserDetails(pid)))
      )
    }
}
