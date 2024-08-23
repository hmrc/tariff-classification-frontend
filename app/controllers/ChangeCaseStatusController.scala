/*
 * Copyright 2024 HM Revenue & Customs
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
import models.forms.CaseStatusRadioInputFormProvider
import models.{CaseStatusRadioInput, Permission}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.CasesService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.change_case_status

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future.successful
//
class ChangeCaseStatusController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  val change_case_status: change_case_status,
  implicit val appConfig: AppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with RenderCaseAction
    with WithUnsafeDefaultFormBinding {

  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService

  val form = new CaseStatusRadioInputFormProvider()()

  def onPageLoad(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.EDIT_RULING)).async { implicit request =>
      validateAndRenderView(c => successful(change_case_status(c, form)))
    }

  def onSubmit(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.EDIT_RULING)) { implicit request =>
      form
        .bindFromRequest()
        .fold(
          hasErrors => Ok(change_case_status(request.`case`, hasErrors)),
          {
            case CaseStatusRadioInput.Complete => Redirect(routes.CompleteCaseController.completeCase(reference))
            case CaseStatusRadioInput.Refer    => Redirect(routes.ReferCaseController.getReferCaseReason(reference))
            case CaseStatusRadioInput.Reject   => Redirect(routes.RejectCaseController.getRejectCaseReason(reference))
            case CaseStatusRadioInput.Suspend  => Redirect(routes.SuspendCaseController.getSuspendCaseReason(reference))
            case CaseStatusRadioInput.MoveBackToQueue =>
              Redirect(routes.ReassignCaseController.reassignCase(reference, request.uri))
            case _ => Redirect(routes.ChangeCaseStatusController.onPageLoad(reference))
          }
        )
    }
}
