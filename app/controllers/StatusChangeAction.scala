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
import models.{Case, Operator, Permission}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

trait StatusChangeAction[T] extends RenderCaseAction { this: FrontendController =>

  protected val verify: RequestActions

  protected val form: Form[T]

  protected def status(c: Case): T

  protected def chooseStatusView(c: Case, preFilledForm: Form[T], options: Option[String] = None)(implicit request: Request[_]): Html

  protected def update(c: Case, status: T, operator: Operator)(implicit hc: HeaderCarrier): Future[Case]

  protected def onSuccessRedirect(reference: String, isV2Liability: Boolean = false): Call

  protected val requiredPermission: Permission

  implicit val config: AppConfig

  def chooseStatus(reference: String, options: Option[String] = None): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(requiredPermission)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(chooseStatusView(c, form.fill(status(c)), options))
    )
  }

  def updateStatus(reference: String, options: Option[String] = None): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(requiredPermission)).async { implicit request =>
    form.bindFromRequest().fold(
      errors => {
        getCaseAndRenderView(
          reference,
          c => successful(chooseStatusView(c, errors, options))
        )
      },
      (status: T) => {
        getCaseAndRespond(
          reference,
          c => {
            val isV2Liability = config.newLiabilityDetails && !c.application.isBTI
            if (statusHasChanged(c, status)) {
              update(c, status, request.operator).flatMap { _ =>
                successful(Redirect(onSuccessRedirect(reference, isV2Liability)))
              }
            } else {
              successful(Redirect(onSuccessRedirect(reference, isV2Liability)))
            }
          }
        )
      }
    )

  }

  private def statusHasChanged(c: Case, updated: T): Boolean = {
    status(c) != updated
  }

}
