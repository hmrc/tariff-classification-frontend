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
import uk.gov.hmrc.tariffclassificationfrontend.forms.{CancelRulingForm, ReviewForm}
import uk.gov.hmrc.tariffclassificationfrontend.models.CancelReason.CancelReason
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.COMPLETED
import uk.gov.hmrc.tariffclassificationfrontend.models.ReviewStatus.ReviewStatus
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class CancelRulingController @Inject()(authenticatedAction: AuthenticatedAction,
                                       casesService: CasesService,
                                       val messagesApi: MessagesApi,
                                       implicit val appConfig: AppConfig) extends RenderCaseAction {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  override protected def redirect: String => Call = routes.CaseController.applicationDetails

  override protected def isValidCase: Case => Boolean = _.status == COMPLETED

  def cancelRuling(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => {
        val existingReason = c.decision.flatMap(_.cancelReason)
        val form = CancelRulingForm.form.fill(existingReason)
        successful(views.html.cancel_ruling(c, form))
      }
    )
  }

  def confirmCancelRuling(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>

    CancelRulingForm.form.bindFromRequest().fold(
      errors => {
        getCaseAndRenderView(
          reference,
          c => successful(views.html.cancel_ruling(c, errors))
        )
      },
      (reason: Option[CancelReason]) => {
        getCaseAndRenderView(
          reference,
          c => {
            // TODO - pass reason to service to persist
            println(s"reason was $reason")
            caseService.cancelRuling(c, request.operator).map(views.html.confirm_cancel_ruling(_))
          }
        )
      }
    )
  }

}
