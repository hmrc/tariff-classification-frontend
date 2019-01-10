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
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.OPEN
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class CompleteCaseController @Inject()(authenticatedAction: AuthenticatedAction,
                                       casesService: CasesService,
                                       val messagesApi: MessagesApi,
                                       implicit val appConfig: AppConfig) extends CaseAction {

  override protected val caseService: CasesService = casesService
  override protected val config: AppConfig = appConfig
  override protected lazy val redirect: String => Call = routes.CaseController.applicationDetails
  override protected def isValidCase: Case => Boolean = c => c.status == OPEN && c.decision.isDefined

  def completeCase(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      caseReference = reference,
      toHtml = c => successful(views.html.complete_case(c))
    )
  }

  def confirmCompleteCase(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      caseReference = reference,
      casesService.completeCase(_, request.operator).map(views.html.confirm_complete_case(_))
    )
  }

}
