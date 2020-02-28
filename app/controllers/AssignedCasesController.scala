/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import config.AppConfig
import controllers.SessionKeys._
import controllers.routes.AssignedCasesController
import javax.inject.{Inject, Singleton}
import models.request.AuthenticatedRequest
import models.{NoPagination, Permission}
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AssignedCasesController @Inject()(
  verify: RequestActions,
  casesService: CasesService,
  queuesService: QueuesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc) with I18nSupport {

  def assignedCases(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.VIEW_ASSIGNED_CASES))
    .async { implicit request =>
      showAssignedCases()
    }

  def assignedCasesFor(assigneeId: String, startAtTabIndex : Int): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.VIEW_ASSIGNED_CASES))
    .async { implicit request =>
      showAssignedCases(Some(assigneeId),startAtTabIndex = startAtTabIndex)
    }

  private def showAssignedCases(assigneeId: Option[String] = None, startAtTabIndex : Int = 0)
                               (implicit request: AuthenticatedRequest[_]): Future[Result] = {
    for {
      cases <- casesService.getAssignedCases(NoPagination())
      queues <- queuesService.getAll
      caseCountByQueue <- casesService.countCasesByQueue(request.operator)
    } yield Ok(views.html.assigned_cases(queues, cases.results, assigneeId, caseCountByQueue, startAtTabIndex))
              .addingToSession((backToQueuesLinkLabel, request2Messages(implicitly)("cases.menu.assigned-cases")),
                               (backToQueuesLinkUrl, assigneeId.map(AssignedCasesController.assignedCasesFor(_,startAtTabIndex).url)
                                                               .getOrElse(AssignedCasesController.assignedCases().url)))
              .removingFromSession(backToSearchResultsLinkLabel, backToSearchResultsLinkUrl)

  }

}
