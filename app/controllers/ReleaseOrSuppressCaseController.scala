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

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import config.AppConfig
import models.forms.CaseStatusRadioInputFormProvider
import models.{CaseStatusRadioInput, Permission}
import service.CasesService

import scala.concurrent.Future.successful

class ReleaseOrSuppressCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  val release_or_suppress: views.html.release_or_suppress,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with RenderCaseAction {

  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService

  val form = new CaseStatusRadioInputFormProvider()()

  def onPageLoad(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHaveOneOf(Seq(Permission.SUPPRESS_CASE, Permission.RELEASE_CASE))).async { implicit request =>
      validateAndRenderView(c => successful(release_or_suppress(c, form)))
    }

  def onSubmit(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHaveOneOf(Seq(Permission.SUPPRESS_CASE, Permission.RELEASE_CASE))) { implicit request =>
      form.bindFromRequest.fold(
        hasErrors => Ok(release_or_suppress(request.`case`, hasErrors)), {
          case CaseStatusRadioInput.Release =>
            Redirect(routes.ReleaseCaseController.releaseCase(reference))
          case CaseStatusRadioInput.Suppress =>
            Redirect(routes.SuppressCaseController.getSuppressCaseReason(reference))
        }
      )
    }
}
