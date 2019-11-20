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

import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.CaseStatusRadioInputFormProvider
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatusRadioInput, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views.html.change_case_status

import scala.concurrent.Future.successful

class ChangeCaseStatusController @Inject()(verify: RequestActions,
                                           casesService: CasesService,
                                           val messagesApi: MessagesApi,
                                           implicit val appConfig: AppConfig) extends RenderCaseAction {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  val form = new CaseStatusRadioInputFormProvider().apply()

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
      form.bindFromRequest.fold(
        hasErrors => Ok(change_case_status(request.`case`, hasErrors)),
        {
          case CaseStatusRadioInput.Complete        => Redirect(routes.CompleteCaseController.completeCase(reference))
          case CaseStatusRadioInput.Refer           => Redirect(routes.ReferCaseController.getReferCase(reference))
          case CaseStatusRadioInput.Reject          => Redirect(routes.RejectCaseController.getRejectCase(reference))
          case CaseStatusRadioInput.Suspend         => Redirect(routes.SuspendCaseController.getSuspendCase(reference))
          case CaseStatusRadioInput.MoveBackToQueue => Redirect(routes.ReassignCaseController.reassignCase(reference, request.uri))
        }
      )
    }
}