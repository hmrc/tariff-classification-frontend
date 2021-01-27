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
import models.viewmodels._
import models.viewmodels.managementtools.UsersTabViewModel
import models.{ApplicationType, NoPagination, Permission, Queues}
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global

class ManageUsersController @Inject() (
  verify: RequestActions,
  mcc: MessagesControllerComponents,
  val manageUsersView: views.html.managementtools.manage_users_view,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  //todo add main and secondary navigation tabs
  def displayManageUsers(): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_CASES))( //todo verify permission for manager
      implicit request =>
        Ok(manageUsersView(UsersTabViewModel.forManagedTeams(
          Queues.allQueues
          //Seq(Queues.act, Queues.cap ).toList //todo replace dummy stub with a query
        )))
    )
}
