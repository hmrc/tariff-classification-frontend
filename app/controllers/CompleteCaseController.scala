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
import models.forms.DecisionForm
import javax.inject.{Inject, Singleton}
import models._
import models.forms.v2.{CompleteCaseChoiceForm, LiabilityDetailsForm}
import models.request.AuthenticatedRequest
import play.api.data.Form
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class CompleteCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  decisionForm: DecisionForm,
  liabilityDetailsForm: LiabilityDetailsForm,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with RenderCaseAction {

  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService
  private lazy val completeCaseForm: Form[Boolean] = CompleteCaseChoiceForm.form

  def completeCase(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.COMPLETE_CASE))
      .async { implicit request =>
        validateAndRespond(c =>
          c.application.`type` match {
            case ApplicationType.ATAR =>
              successful(Ok(views.html.complete_case(c, completeCaseForm)))

            case ApplicationType.LIABILITY | ApplicationType.CORRESPONDENCE | ApplicationType.MISCELLANEOUS =>
              casesService
                .completeCase(c, request.operator)
                .map(c => Redirect(routes.CompleteCaseController.confirmCompleteCase(c.reference)))
          }
        )
      }

  def postCompleteCase(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.COMPLETE_CASE))
      .async { implicit request =>
        completeCaseForm
          .bindFromRequest()
          .fold(
            errors => validateAndRespond(c => successful(Ok(views.html.complete_case(c, errors)))), {
              case true =>
                validateAndRedirect(
                  casesService
                    .completeCase(_, request.operator)
                    .map(c => routes.CompleteCaseController.confirmCompleteCase(c.reference))
                )
              case false => successful(Redirect(routes.CaseController.rulingDetails(reference)))
            }
          )

      }

  def confirmCompleteCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      renderView(c => c.status == CaseStatus.COMPLETED, c => successful(views.html.confirm_complete_case(c)))
    }

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = hasValidDecision(c)

  private def hasValidDecision(c: Case): Boolean = c.application.`type` match {
    case ApplicationType.ATAR =>
      decisionForm.bindFrom(c.decision).map(_.errors).exists(_.isEmpty)
    case ApplicationType.LIABILITY =>
      liabilityDetailsForm.liabilityDetailsCompleteForm(c).errors.isEmpty && decisionForm
        .liabilityCompleteForm(c.decision.getOrElse(Decision()))
        .errors
        .isEmpty
    case _ =>
      true
  }

}
