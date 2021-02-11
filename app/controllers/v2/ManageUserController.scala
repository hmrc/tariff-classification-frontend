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
import models.forms.v2.RemoveUserForm
import models.request.AuthenticatedRequest
import models.viewmodels._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, EventsService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import models.viewmodels.{ManagerToolsUsersTab, SubNavigationTab}
import play.api.data.Form

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class ManageUserController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  userService: UserService,
  mcc: MessagesControllerComponents,
  val viewUser: views.html.partials.users.view_user,
  val cannotDeleteUser: views.html.partials.users.cannot_delete_user,
  val confirmDeleteUser: views.html.partials.users.confirm_delete_user
)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private lazy val removeUserForm: Form[Boolean] = RemoveUserForm.form

  def displayUserDetals(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userTab <- userService.getUser(pid)
          cases   <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          userCaseTabs = ApplicationsTab.casesByTypes(cases.results)
        } yield Ok(viewUser(userTab, userCaseTabs))
    }

  def deleteUser(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userCases <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          user      <- userService.getUser(pid)
        } yield {
          if (userCases.results.nonEmpty) {
            Ok(cannotDeleteUser(user))
          } else {
            Ok(confirmDeleteUser(user, removeUserForm))
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
            } yield Ok(confirmDeleteUser(user, errors)), {
            case true =>
              for {
                user <- userService.getUser(pid)
              } yield {
                val updatedUser = user.copy(deleted = true)
                userService
                  .updateUser(updatedUser, request.operator)
                Redirect(controllers.v2.routes.ManageUsersController.displayManageUsers())
              }
            case _ =>
              successful(
                Redirect(controllers.v2.routes.ManageUserController.displayUserDetals(pid))
              )
          }
        )
    )

}
