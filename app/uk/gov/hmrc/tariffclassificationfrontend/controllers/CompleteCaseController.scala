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
import uk.gov.hmrc.tariffclassificationfrontend.forms.{DecisionForm, LiabilityDetailsForm}
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.OPEN
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class CompleteCaseController @Inject()(verify: RequestActions,
                                       casesService: CasesService,
                                       decisionForm: DecisionForm,
                                       val messagesApi: MessagesApi,
                                       implicit val appConfig: AppConfig) extends RenderCaseAction {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  def completeCase(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.COMPLETE_CASE)).async { implicit request =>
    validateAndRespond(c =>
      c.application.`type` match {
        case ApplicationType.BTI =>
          successful(Ok(views.html.complete_case(c)))

        case ApplicationType.LIABILITY_ORDER =>
          casesService.completeCase(c, request.operator)
            .map(c => Redirect(routes.CompleteCaseController.confirmCompleteCase(c.reference)))
      }
    )
  }

  def postCompleteCase(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.COMPLETE_CASE)).async { implicit request =>
    validateAndRedirect(casesService.completeCase(_, request.operator)
      .map(c => routes.CompleteCaseController.confirmCompleteCase(c.reference)))
  }

  def confirmCompleteCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      renderView(c => c.status == CaseStatus.COMPLETED, c => successful(views.html.confirm_complete_case(c)))
    }

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    println("overriden method :::::::::::::::::::::::::::")
    println("overriden method :::::::::::::::::::::::::::")
    println("overriden method :::::::::::::::::::::::::::")
    println("overriden method :::::::::::::::::::::::::::")
    println("overriden method :::::::::::::::::::::::::::")
    hasValidDecision(c)
  }

  private def hasValidDecision(c: Case): Boolean = c.application.`type` match {
    case ApplicationType.BTI =>
      decisionForm.bindFrom(c.decision).map(_.errors).exists(_.isEmpty)
    case ApplicationType.LIABILITY_ORDER =>
      decisionForm.liabilityCompleteForm(c.decision.getOrElse(Decision())).errors.isEmpty &&
        LiabilityDetailsForm.liabilityDetailsCompleteForm(c.application.asLiabilityOrder).errors.isEmpty
  }

}
