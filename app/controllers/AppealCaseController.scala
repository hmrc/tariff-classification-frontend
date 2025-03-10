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
import models.AppealStatus.AppealStatus
import models.AppealType.AppealType
import models._
import models.forms.AppealForm
import play.api.data.Form
import play.api.mvc._
import services.CasesService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{appeal_change_status, appeal_choose_status, appeal_choose_type}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppealCaseController @Inject() (
  verify: RequestActions,
  override val caseService: CasesService,
  override implicit val config: AppConfig,
  val appeal_choose_status: appeal_choose_status,
  val appeal_choose_type: appeal_choose_type,
  val appeal_change_status: appeal_change_status,
  mcc: MessagesControllerComponents
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with RenderCaseAction
    with WithUnsafeDefaultFormBinding {

  private val typeForm: Form[AppealType]     = AppealForm.appealTypeForm
  private val statusForm: Form[AppealStatus] = AppealForm.appealStatusForm

  def appealDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.ATAR =>
          Future(Redirect(v2.routes.AtarController.displayAtar(reference).withFragment(Tab.APPEALS_TAB.name)))
        case ApplicationType.LIABILITY =>
          Future(
            Redirect(v2.routes.LiabilityController.displayLiability(reference).withFragment(Tab.APPEALS_TAB.name))
          )
      }
    }

  def chooseType(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
      getCaseAndRenderView(
        reference,
        c => Future(appeal_choose_type(c, typeForm))
      )
    }

  def confirmType(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
      getCaseAndRespond(
        reference,
        `case` =>
          typeForm
            .bindFromRequest()
            .fold(
              formWithErrors => Future(Ok(appeal_choose_type(`case`, formWithErrors))),
              appealType =>
                Future(Redirect(routes.AppealCaseController.chooseStatus(`case`.reference, appealType.toString)))
            )
      )
    }

  def chooseStatus(reference: String, appealType: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
      val appealTypeFound = AppealType.withName(appealType)
      getCaseAndRenderView(
        reference,
        c => successful(appeal_choose_status(c, appealTypeFound, statusForm))
      )
    }

  def changeStatus(reference: String, appealId: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
      getCaseAndRespond(
        reference,
        c =>
          c.findAppeal(appealId) match {
            case Some(appeal) => Future(Ok(appeal_change_status(c, appeal, statusForm)))
            case None         => Future(Redirect(routes.AppealCaseController.appealDetails(reference)))
          }
      )
    }

  def confirmStatus(reference: String, appealType: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
      val appealTypeFound = AppealType.withName(appealType)
      getCaseAndRespond(
        reference,
        `case` =>
          statusForm
            .bindFromRequest()
            .fold(
              formWithErrors => Future(Ok(appeal_choose_status(`case`, appealTypeFound, formWithErrors))),
              appealStatus =>
                caseService
                  .addAppeal(`case`, appealTypeFound, appealStatus, request.operator)
                  .map(_ => Redirect(routes.AppealCaseController.appealDetails(reference)))
            )
      )
    }

  def confirmChangeStatus(reference: String, appealId: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
      getCaseAndRespond(
        reference,
        `case` =>
          `case`.findAppeal(appealId) match {
            case Some(appeal) =>
              statusForm
                .bindFromRequest()
                .fold(
                  formWithErrors => Future(Ok(appeal_change_status(`case`, appeal, formWithErrors))),
                  appealStatus =>
                    caseService
                      .updateAppealStatus(`case`, appeal, appealStatus, request.operator)
                      .map(_ => Redirect(routes.AppealCaseController.appealDetails(reference)))
                )
            case None => Future(Redirect(redirect(request.`case`.reference)))
          }
      )
    }

}
