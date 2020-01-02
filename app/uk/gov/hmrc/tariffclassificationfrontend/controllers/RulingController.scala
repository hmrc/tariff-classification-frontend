/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{DecisionForm, DecisionFormData, DecisionFormMapper}
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService}
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RulingController @Inject()(verify: RequestActions,
                                 casesService: CasesService,
                                 fileStoreService: FileStoreService,
                                 mapper: DecisionFormMapper,
                                 decisionForm: DecisionForm,
                                 val messagesApi: MessagesApi,
                                 implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private final val rulingDetailsStartTabIndex = 7000

  def editRulingDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.EDIT_RULING)).async { implicit request =>
    getCaseAndThen(c => c.application.`type` match {
      case ApplicationType.BTI =>
        val formData = mapper.caseToDecisionFormData(c)
        val df = decisionForm.btiForm.fill(formData)
        editBTIRulingView(df, c)

      case ApplicationType.LIABILITY_ORDER =>
        val decision = c.decision.getOrElse(Decision())
        val df = decisionForm.liabilityForm(decision)
        editLiabilityRulingView(df, c)
    })
  }

  def validateBeforeComplete(reference: String):  Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.EDIT_RULING)).async { implicit request =>
    getCaseAndThen(c => c.application.`type` match {
      case ApplicationType.BTI =>
        val formData = mapper.caseToDecisionFormData(c)
        val decisionFormWithErrors = decisionForm.btiCompleteForm.fillAndValidate(formData)
        editBTIRulingView(decisionFormWithErrors, c)
    })
  }

  def updateRulingDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.EDIT_RULING)).async { implicit request =>
    getCaseAndThen(c => c.application.`type` match {
      case ApplicationType.BTI =>
        decisionForm.btiForm.bindFromRequest.fold(
          errorForm => editBTIRulingView(errorForm, c),
          validForm => for {
            update <- casesService.updateCase(mapper.mergeFormIntoCase(c, validForm))
          } yield Redirect(routes.CaseController.rulingDetails(update.reference))
        )

      case ApplicationType.LIABILITY_ORDER =>
        val decision = c.decision.getOrElse(Decision())
        decisionForm.liabilityForm(decision).bindFromRequest.fold(
          errorForm => editLiabilityRulingView(errorForm, c),
          updatedDecision => for {
            update <- casesService.updateCase(c.copy(decision = Some(updatedDecision)))
          } yield Redirect(routes.LiabilityController.liabilityDetails(update.reference))
        )
    })
  }

  private def editBTIRulingView(f: Form[DecisionFormData], c: Case)(implicit request: AuthenticatedRequest[_]): Future[Result] = {
    fileStoreService.getAttachments(c).map(views.html.ruling_details_edit(c, _, f, startAtTabIndex = Some(rulingDetailsStartTabIndex))).map(Ok(_))
  }

  private def editLiabilityRulingView(f: Form[Decision], c: Case)(implicit request: AuthenticatedRequest[_]): Future[Result] = {
    Future.successful(Ok(views.html.edit_liability_decision(c, f)))
  }

  private def getCaseAndThen(toResult: Case => Future[Result])
                                (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    toResult(request.`case`)
  }

}
