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
import models.forms.v2.UserEditTeamForm
import models.request.AuthenticatedRequest
import models.viewmodels.{ManagerToolsUsersTab, SubNavigationTab, _}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, EventsService, UserService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class ManageUserController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  eventsService: EventsService,
  userService: UserService,
  mcc: MessagesControllerComponents,
  val viewUser: views.html.partials.users.view_user,
  val user_team_edit: views.html.partials.users.user_team_edit
)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  private val userEditTeamform: Form[List[String]] = UserEditTeamForm.newTeamForm

  private def getReferralEvents(
    cases: Paged[Case]
  )(implicit hc: HeaderCarrier): Future[Map[String, Event]] =
    cases.results.toList
      .traverse { aCase =>
        eventsService.getFilteredEvents(aCase.reference, NoPagination(), Some(Set(EventType.CASE_REFERRAL))).map {
          events =>
            val eventsLatestFirst = events.results.sortBy(_.timestamp)(Event.latestFirst)
            val latestReferralEvent = eventsLatestFirst.collectFirst {
              case event @ Event(_, _, _, caseReference, _) => Map(caseReference -> event)
              case _                                        => Map.empty
            }
            latestReferralEvent.getOrElse(Map.empty)
        }
      }
      .map(_.foldLeft(Map.empty[String, Event])(_ ++ _))

  private def getCompletedEvents(
    cases: Paged[Case]
  )(implicit hc: HeaderCarrier): Future[Map[String, Event]] =
    cases.results.toList
      .traverse { aCase =>
        eventsService.findCompletionEvents(Set(aCase.reference), NoPagination()).map { events =>
          val eventsLatestFirst = events.results.sortBy(_.timestamp)(Event.latestFirst)
          val latestCompletedEvent = eventsLatestFirst.collectFirst {
            case event @ Event(_, _, _, caseReference, _) => Map(caseReference -> event)
            case _                                        => Map.empty
          }
          latestCompletedEvent.getOrElse(Map.empty)
        }
      }
      .map(_.foldLeft(Map.empty[String, Event])(_ ++ _))

  def displayUserDetails(pid: String, activeSubNav: SubNavigationTab = ManagerToolsUsersTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          userTab <- userService.getUser(pid)
          cases   <- casesService.getCasesByAssignee(Operator(pid), NoPagination())
          userCaseTabs = ApplicationsTab.casesByTypes(cases.results)
        } yield Ok(viewUser(userTab, userCaseTabs))
    }

  def editUserTeamDetails(pid: String): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_CASES)
      andThen verify.mustHave(Permission.VIEW_REPORTS)).async { implicit request =>
      successful(Ok(user_team_edit(Operator(pid), userEditTeamform)))
    }

  def postEditUserTeams(pid: String): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)).async { implicit request =>
      userEditTeamform.bindFromRequest.fold(
        formWithErrors => Future.successful(Ok(user_team_edit(Operator(pid), formWithErrors))),
        userToBeUpdated =>
          userService.updateUser(Operator(pid, memberOfTeams = userToBeUpdated), request.operator).map { _ =>
            Redirect(routes.ManageUserController.displayUserDetails(pid))
          }
      )
    }
}
