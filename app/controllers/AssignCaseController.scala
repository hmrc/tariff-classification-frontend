/*
 * Copyright 2025 HM Revenue & Customs
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
import models.forms.TakeOwnerShipForm
import models.request.AuthenticatedRequest
import models.{Case, Permission}
import play.api.data.Form
import play.api.mvc._
import services.CasesService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.assign_case

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssignCaseController @Inject() (
  verify: RequestActions,
  override val caseService: CasesService,
  mcc: MessagesControllerComponents,
  val assignCase: assign_case,
  override implicit val config: AppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with RenderCaseAction
    with WithUnsafeDefaultFormBinding {

  private lazy val takeOwnershipForm: Form[Boolean] = TakeOwnerShipForm.form

  def get(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.ASSIGN_CASE))
      .async(implicit request => getCaseAndRenderView(reference, c => Future(assignCase(c, takeOwnershipForm))))

  def post(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.ASSIGN_CASE))
      .async { implicit request =>
        def respond: Case => Future[Result] = {
          case c: Case if c.assignee.isEmpty =>
            caseService.assignCase(c, request.operator).map(_ => Redirect(routes.CaseController.get(reference)))
          case _ =>
            Future(Redirect(routes.AssignCaseController.get(reference)))
        }

        takeOwnershipForm
          .bindFromRequest()
          .fold(
            formWithErrors => getCaseAndRenderView(reference, c => Future(assignCase(c, formWithErrors))),
            {
              case true =>
                getCaseAndRespond(reference, respond)
              case _ =>
                Future(Redirect(controllers.routes.CaseController.get(reference)))
            }
          )

      }

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[?]): Boolean =
    (c.queueId, c.assignee) match {
      case (Some(_), None)                                                 => true
      case (Some(_), Some(operator)) if request.operator.id != operator.id => true
      case _                                                               => false
    }

}
