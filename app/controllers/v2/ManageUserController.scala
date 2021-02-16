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

import cats.syntax.traverse._
import com.google.inject.Inject
import config.AppConfig
import controllers.RequestActions
import models._
import models.forms.v2.MoveCasesForm
import models.request.AuthenticatedRequest
import models.viewmodels._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, EventsService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import models.viewmodels.{ManagerToolsUsersTab, SubNavigationTab}

import scala.concurrent.{ExecutionContext, Future}

class ManageUserController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  userService: UserService,
  mcc: MessagesControllerComponents,
  val viewUser: views.html.partials.users.view_user
)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private val moveATaRCasesForm           = MoveCasesForm.moveCasesForm
  private val moveLiabilityCasesForm      = MoveCasesForm.moveCasesForm
  private val moveMiscCasesForm           = MoveCasesForm.moveCasesForm
  private val moveCorrespondenceCasesForm = MoveCasesForm.moveCasesForm

  def displayUserDetails(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userTab <- userService.getUser(pid)
          cases   <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          userCaseTabs = ApplicationsTab.casesByTypes(cases.results)
        } yield userTab
          .map(user => Ok(viewUser(user, userCaseTabs, moveATaRCasesForm)))
          .getOrElse(NotFound(views.html.user_not_found(pid)))
    }

  def postMoveCases(activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async(implicit request => ???)

}
