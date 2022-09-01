/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.stream.Materializer
import com.google.inject.Inject
import config.AppConfig
import controllers.RequestActions
import models._
import models.request.AuthenticatedRequest
import models.viewmodels._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, EventsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.my_cases_view

import scala.concurrent.ExecutionContext

class MyCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  eventsService: EventsService,
  mcc: MessagesControllerComponents,
  val myCasesView: my_cases_view
)(
  implicit val appConfig: AppConfig,
  mat: Materializer
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  implicit val ec: ExecutionContext = mat.executionContext

  def displayMyCases(activeSubNav: SubNavigationTab = AssignedToMeTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_MY_CASES)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          cases <- casesService.getCasesByAssignee(request.operator, NoPagination())
          caseReferences = cases.results.map(_.reference).toSet
          referralEventsByCase <- eventsService.findReferralEvents(caseReferences)
          completeEventsByCase <- eventsService.findCompletionEvents(caseReferences)
          myCaseStatuses = activeSubNav match {
            case AssignedToMeTab  => ApplicationsTab.assignedToMeCases(cases.results)
            case ReferredByMeTab  => ApplicationsTab.referredByMe(cases.results, referralEventsByCase)
            case CompletedByMeTab => ApplicationsTab.completedByMe(cases.results, completeEventsByCase)
          }
        } yield Ok(myCasesView(myCaseStatuses, activeSubNav))
    }
}
