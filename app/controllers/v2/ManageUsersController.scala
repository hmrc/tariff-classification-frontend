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
import models.viewmodels._
import models.viewmodels.managementtools.UsersTabViewModel
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import cats.syntax.all._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManageUsersController @Inject()(
  verify: RequestActions,
  mcc: MessagesControllerComponents,
  userService: UserService,
  casesService: CasesService,
  val manageUsersView: views.html.managementtools.manage_users_view,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  val Unassigned = "unassigned"

  def displayManageUsers(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      for {
        manager <- userService.getUser(request.operator.id)
        managerQueues = manager.memberOfTeams.flatMap(id => Queues.queueById(id))
        allUsers          <- userService.getAllUsers(Role.CLASSIFICATION_OFFICER, "some", NoPagination())
        managerTeamsCases <- casesService.getCasesByAllQueues2(managerQueues, NoPagination())
        usersWithCount = managerTeamsCases.results.toList
          .groupBy(singleCase => singleCase.assignee.map(_.id).getOrElse(Unassigned))
          .filterKeys(_ != Unassigned)
        usersTabViewModel = UsersTabViewModel.fromUsers(manager, allUsers)
      } yield Ok(manageUsersView(activeSubNav, usersTabViewModel, usersWithCount))
    }
}
