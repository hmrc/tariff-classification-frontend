/*
 * Copyright 2023 HM Revenue & Customs
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
import models.Permission
import models.request.AuthenticatedCaseRequest
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Notification._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ReopenCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with RenderCaseAction
    with WithUnsafeDefaultFormBinding {

  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService

  def confirmReopenCase(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.REOPEN_CASE)).async { implicit request: AuthenticatedCaseRequest[AnyContent] =>
      validateAndRedirect(_ =>
        casesService
          .reopenCase(request.`case`, request.operator)
          .map(updatedCase => routes.CaseController.get(updatedCase.reference))
      ).map(result => result.flashing(success("notification.success.referral.off")))
    }
}
