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
import models.forms.AppealForm
import javax.inject.{Inject, Singleton}
import models.AppealStatus.AppealStatus
import models.AppealType.AppealType
import models._
import play.api.data.Form
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class AppealCaseController @Inject()(
  verify: RequestActions,
  override val caseService: CasesService,
  override implicit val config: AppConfig,
  mcc: MessagesControllerComponents
) extends FrontendController(mcc) with RenderCaseAction {

  private val typeForm: Form[AppealType] = AppealForm.appealTypeForm
  private val statusForm: Form[AppealStatus] = AppealForm.appealStatusForm
  private lazy val newliabilityDetailsToggle = config.newLiabilityDetails

  private val startTabIndexForAppeals = 8000


  def appealDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>

    request.`case`.application.`type` match {
      case ApplicationType.BTI =>
        getCaseAndRenderView(reference,
          c => successful(views.html.case_details(
            c, CaseDetailPage.APPEAL, views.html.partials.appeal.appeal_details(
              c, startTabIndexForAppeals), activeTab = Some(ActiveTab.Appeals)))
        )

      case ApplicationType.LIABILITY_ORDER => {
        if (newliabilityDetailsToggle) successful(
          Redirect(v2.routes.LiabilityController.displayLiability(reference).withFragment("appeal_tab")))
        else getCaseAndRenderView(reference,
          c => successful(views.html.case_details(
            c, CaseDetailPage.APPEAL, views.html.partials.appeal.appeal_details(
              c, startTabIndexForAppeals), activeTab = Some(ActiveTab.Appeals)))
        )
      }
    }
  }

  def chooseType(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)
    andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(views.html.appeal_choose_type(c, typeForm))
    )
  }

  def confirmType(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)
    andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    getCaseAndRespond(
      reference,
      `case` => typeForm.bindFromRequest().fold(
        formWithErrors => Future.successful(Ok(views.html.appeal_choose_type(`case`, formWithErrors))),
        appealType => Future.successful(Redirect(routes.AppealCaseController.chooseStatus(`case`.reference, appealType.toString)))
      )
    )
  }

  def chooseStatus(reference: String, appealType: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)
    andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    val appealTypeFound = AppealType.withName(appealType)
    getCaseAndRenderView(
      reference,
      c =>
        successful(views.html.appeal_choose_status(c, appealTypeFound, statusForm))
    )
  }

  def changeStatus(reference: String, appealId: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)
    andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => {
        c.findAppeal(appealId) match {
          case Some(appeal) => successful(views.html.appeal_change_status(c, appeal, statusForm))
          case None => successful(views.html.case_details(c, CaseDetailPage.APPEAL, views.html.partials.appeal.appeal_details(c)))
        }
      }
    )
  }

  def confirmStatus(reference: String, appealType: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)
    andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    val appealTypeFound = AppealType.withName(appealType)
    getCaseAndRespond(
      reference,
      `case` => statusForm.bindFromRequest().fold(
        formWithErrors => successful(Ok(views.html.appeal_choose_status(`case`, appealTypeFound, formWithErrors))),
        appealStatus => caseService.addAppeal(`case`, appealTypeFound, appealStatus, request.operator)
          .map(_ => Redirect(routes.AppealCaseController.appealDetails(reference)))
      )
    )
  }

  def confirmChangeStatus(reference: String, appealId: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)
    andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    getCaseAndRespond(
      reference,
      `case` => {
        `case`.findAppeal(appealId) match {
          case Some(appeal) => statusForm.bindFromRequest().fold(
            formWithErrors => successful(Ok(views.html.appeal_change_status(`case`, appeal, formWithErrors))),
            appealStatus => caseService.updateAppealStatus(`case`, appeal, appealStatus, request.operator)
              .map(_ => Redirect(routes.AppealCaseController.appealDetails(reference)))
          )
          case None => successful(Redirect(redirect(request.`case`.reference)))
        }
      }
    )
  }

}
