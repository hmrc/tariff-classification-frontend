/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.controllers.SessionKeys._
import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes.AssignedCasesController
import uk.gov.hmrc.tariffclassificationfrontend.models.{NoPagination, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AssignedCasesController @Inject()(verify: RequestActions,
                                        casesService: CasesService,
                                        queuesService: QueuesService,
                                        val messagesApi: MessagesApi,
                                        implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def assignedCases(): Action[AnyContent] = (verify.authenticate andThen verify.mustHave(Permission.VIEW_ASSIGNED_CASES))
    .async { implicit request =>
    showAssignedCases()
  }

  def assignedCasesFor(assigneeId: String): Action[AnyContent] = (verify.authenticate andThen verify.mustHave(Permission.VIEW_ASSIGNED_CASES))
    .async { implicit request =>
    showAssignedCases(Some(assigneeId))
  }

  private def showAssignedCases(assigneeId: Option[String] = None)
                               (implicit request: AuthenticatedRequest[_]): Future[Result] = {
    for {
      cases <- casesService.getAssignedCases(NoPagination())
      queues <- queuesService.getAll
    } yield Ok(views.html.assigned_cases(queues, cases.results, assigneeId))
              .addingToSession((backToQueuesLinkLabel, messagesApi("cases.menu.assigned-cases")),
                               (backToQueuesLinkUrl, assigneeId.map(AssignedCasesController.assignedCasesFor(_).url)
                                                               .getOrElse(AssignedCasesController.assignedCases().url)))
              .removingFromSession(backToSearchResultsLinkLabel, backToSearchResultsLinkUrl)

  }

}
