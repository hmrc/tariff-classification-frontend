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
import controllers.routes.SecurityController
import models._
import models.forms.v2.{MoveCasesForm, TeamOrUserForm}
import models.request.AuthenticatedRequest
import models.viewmodels.{ManagerToolsUsersTab, SubNavigationTab, _}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.HeaderNames

class MoveCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  userService: UserService,
  dataCacheConnector: DataCacheConnector,
  mcc: MessagesControllerComponents,
  val teamOrUserPage: views.html.partials.users.move_cases_team_or_user
)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private val moveATaRCasesForm = MoveCasesForm.moveCasesForm
  private val MoveCasesCacheKey = "move_cases"
  private val ChosenCases       = "chosen_cases"
  private val teamOrUserForm    = TeamOrUserForm.form

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
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async(implicit request => ???)

}
