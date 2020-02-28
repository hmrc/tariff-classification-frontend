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

package controllers

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.request.AuthenticatedRequest
import models.{Case, Permission}
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class AssignCaseController @Inject()(
  verify: RequestActions,
  override val caseService: CasesService,
  mcc: MessagesControllerComponents,
  override implicit val config: AppConfig
) extends FrontendController(mcc) with RenderCaseAction {

  def get(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.ASSIGN_CASE)).async { implicit request =>
    getCaseAndRenderView(reference, c => successful(views.html.assign_case(c)))
  }

  def post(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.ASSIGN_CASE)).async { implicit request =>

    def respond: Case => Future[Result] = {
      case c: Case if c.assignee.isEmpty =>
        caseService.assignCase(c, request.operator).map(_ => Redirect(routes.CaseController.get(reference)))
      case _ =>
        successful(Redirect(routes.AssignCaseController.get(reference)))
    }

    getCaseAndRespond(reference, respond)
  }

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    (c.queueId, c.assignee) match {
      case (Some(_), None) => true
      case (Some(_), Some(operator)) if request.operator.id != operator.id => true
      case _ => false
    }
  }

}
