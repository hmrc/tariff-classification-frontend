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
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.CancelRulingForm
import uk.gov.hmrc.tariffclassificationfrontend.models.CancelReason.CancelReason
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.COMPLETED
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class CancelRulingController @Inject()(authenticatedAction: AuthenticatedAction,
                                       casesService: CasesService,
                                       val messagesApi: MessagesApi,
                                       implicit val appConfig: AppConfig) extends RenderCaseAction {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  override protected def redirect: String => Call = routes.CaseController.applicationDetails

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    c.status == COMPLETED
  }

  private def cancelRuling(f: Form[CancelReason], caseRef: String)
                          (implicit request: AuthenticatedRequest[_]): Future[Result] = {
    getCaseAndRenderView(caseRef, c => successful(views.html.cancel_ruling(c, f)))
  }

  def cancelRuling(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    cancelRuling(CancelRulingForm.form, reference)
  }

  def confirmCancelRuling(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    CancelRulingForm.form.bindFromRequest().fold(
      cancelRuling(_, reference),
      (reason: CancelReason) =>
        getCaseAndRenderView(
          reference,
          caseService.cancelRuling(_, reason, request.operator).map(views.html.confirm_cancel_ruling(_))
        )
    )
  }

}
