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

package controllers

import config.AppConfig
import models._
import models.request.AuthenticatedRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.CasesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.operator_dashboard_classification

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class OperatorDashboardController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  operator_dashboard_classification: operator_dashboard_classification,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.VIEW_MY_CASES)).async {
    implicit request: AuthenticatedRequest[AnyContent] =>
      for {
        casesByAssignee <- casesService.getCasesByAssignee(request.operator, NoPagination())
        casesByQueue    <- casesService.countCasesByQueue
        totalCasesAssignedToMe = casesByAssignee.results.count(c =>
          c.status == CaseStatus.OPEN
        )
        referredCasesAssignedToMe = casesByAssignee.results.count(c =>
          c.status == CaseStatus.REFERRED || c.status == CaseStatus.SUSPENDED
        )
        completedCasesAssignedToMe = casesByAssignee.results.count(c =>
          c.status == CaseStatus.COMPLETED
        )
      } yield Ok(
        operator_dashboard_classification(
          casesByQueue,
          totalCasesAssignedToMe,
          referredCasesAssignedToMe,
          completedCasesAssignedToMe
        )
      )
  }

}
