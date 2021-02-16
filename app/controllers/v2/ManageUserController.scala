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

import config.AppConfig
import controllers.RequestActions
import models._
import models.request.AuthenticatedRequest
import models.viewmodels._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import models.viewmodels.{ManagerToolsUsersTab, SubNavigationTab}

import scala.concurrent.{ExecutionContext}
import models.forms.v2.RemoveUserForm
import play.api.data.Form
import javax.inject.{Inject}
import scala.concurrent.Future.successful

class ManageUserController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  userService: UserService,
  mcc: MessagesControllerComponents,
  val viewUser: views.html.partials.users.view_user,
  val cannotDeleteUser: views.html.partials.users.cannot_delete_user,
  val confirmDeleteUser: views.html.partials.users.confirm_delete_user,
  val doneDeleteUserPage: views.html.partials.users.done_delete_user,
  implicit val appConfig: AppConfig
)(
  implicit val ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private lazy val removeUserForm: Form[Boolean] = RemoveUserForm.form

  def displayUserDetails(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userTab <- userService.getUser(pid)
          cases   <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          userCaseTabs = ApplicationsTab.casesByTypes(cases.results)
        } yield userTab
          .map(user => Ok(viewUser(user, userCaseTabs)))
          .getOrElse(NotFound(views.html.user_not_found(pid)))
    }

  def deleteUser(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userCases <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          user      <- userService.getUser(pid)
        } yield {
          user.isDefined match {
            case true => {
              if (userCases.results.nonEmpty) {
                Ok(cannotDeleteUser(user.get))
              } else {
                Ok(confirmDeleteUser(user.get, removeUserForm))
              }
            }
            case _ => NotFound(views.html.user_not_found(pid))
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
              .getOrElse(NotFound(views.html.user_not_found(pid))), {
            case true =>
              for {
                user <- userService.getUser(pid)
              } yield {
                user.isDefined match {
                  case true => {
                    val updatedUser = user.get.copy(deleted = true)
                    userService
                      .updateUser(updatedUser, request.operator)
                    Redirect(controllers.v2.routes.ManageUserController.doneDeleteUser(updatedUser.safeName))
                  }
                  case _ => NotFound(views.html.user_not_found(pid))
                }

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

}
