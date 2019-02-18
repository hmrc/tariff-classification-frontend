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
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.AppealForm
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus.AppealStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.{CANCELLED, COMPLETED}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class AppealCaseController @Inject()(authenticatedAction: AuthenticatedAction,
                                     override val caseService: CasesService,
                                     override val messagesApi: MessagesApi,
                                     override implicit val config: AppConfig) extends RenderCaseAction {

  override protected def redirect: String => Call = routes.CaseController.trader

  override protected def isValidCase: Case => Boolean = { c: Case =>
    c.status == COMPLETED || c.status == CANCELLED
  }

  def appealDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(views.html.case_details(c, CaseDetailPage.APPEAL, views.html.partials.appeal_details(c)))
    )
  }

  def chooseStatus(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(views.html.change_appeal_status(c, AppealForm.form.fill(c.decision.flatMap(_.appeal).map(_.status))))
    )
  }

  def updateStatus(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    AppealForm.form.bindFromRequest().fold(
      errors => {
        getCaseAndRenderView(
          reference,
          c => successful(views.html.change_appeal_status(c, errors))
        )
      },
      (status: Option[AppealStatus]) => {
        getCaseAndRespond(
          reference,
          c => {
            if (statusHasChanged(c, status)) {
              caseService.updateAppealStatus(c, status, request.operator).flatMap { c =>
                successful(Redirect(routes.AppealCaseController.appealDetails(c.reference)))
              }
            } else {
              successful(Redirect(routes.AppealCaseController.appealDetails(c.reference)))
            }
          }
        )
      }
    )
  }

  private def statusHasChanged(c: Case, status: Option[AppealStatus]): Boolean = {
    c.decision.flatMap(_.appeal).map(_.status) != status
  }

}
