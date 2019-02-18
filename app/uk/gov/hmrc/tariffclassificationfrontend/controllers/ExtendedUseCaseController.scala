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
import uk.gov.hmrc.tariffclassificationfrontend.forms.BooleanForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class ExtendedUseCaseController @Inject()(authenticatedAction: AuthenticatedAction,
                                          override val caseService: CasesService,
                                          override val messagesApi: MessagesApi,
                                          override implicit val config: AppConfig) extends RenderCaseAction {

  override protected def redirect: String => Call = routes.CaseController.trader

  override protected def isValidCase: Case => Boolean = c => CaseStatus.CANCELLED == c.status && c.decision.flatMap(_.cancellation).isDefined

  def chooseStatus(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(views.html.change_extended_use_status(c, BooleanForm.form.fill(c.decision.flatMap(_.cancellation).map(_.applicationForExtendedUse).get)))
    )
  }

  def updateStatus(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    BooleanForm.form.bindFromRequest().fold(
      errors => {
        getCaseAndRenderView(
          reference,
          c => successful(views.html.change_extended_use_status(c, errors))
        )
      },
      (status: Boolean) => {
        getCaseAndRespond(
          reference,
          c => {
            if(statusHasChanged(c, status)) {
              caseService.updateExtendedUseStatus(c, status, request.operator).flatMap { c =>
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

  private def statusHasChanged(c: Case, status: Boolean): Boolean = {
    !c.decision.flatMap(_.cancellation).map(_.applicationForExtendedUse).contains(status)
  }

}
