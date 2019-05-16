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

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.AppealForm
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus.AppealStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealType.AppealType
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.{CANCELLED, COMPLETED}
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class AppealCaseController @Inject()(verify: RequestActions,
                                     override val caseService: CasesService,
                                     override val messagesApi: MessagesApi,
                                     override implicit val config: AppConfig) extends RenderCaseAction {

  private val typeForm: Form[AppealType] = AppealForm.appealTypeForm
  private val statusForm: Form[AppealStatus] = AppealForm.appealStatusForm

  override protected def redirect: String => Call = routes.CaseController.trader

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    (c.status == COMPLETED || c.status == CANCELLED) && c.decision.isDefined
  }

  def appealDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(views.html.case_details(c, CaseDetailPage.APPEAL, views.html.partials.appeal.appeal_details(c)))
    )
  }

  def chooseType(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(views.html.appeal_choose_type(c, typeForm))
    )
  }

  def confirmType(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    getCaseAndRespond(
      reference,
      `case` => typeForm.bindFromRequest().fold(
        formWithErrors => Future.successful(Ok(views.html.appeal_choose_type(`case`, formWithErrors))),
        appealType => Future.successful(Redirect(routes.AppealCaseController.chooseStatus(`case`.reference, appealType.toString)))
      )
    )
  }

  def chooseStatus(reference: String, appealType: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    val appealTypeFound = AppealType.withName(appealType)
    getCaseAndRenderView(
      reference,
      c =>
        successful(views.html.appeal_choose_status(c, appealTypeFound, statusForm))
    )
  }

  def changeStatus(reference: String, appealId: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
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

  def confirmStatus(reference: String, appealType: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    val appealTypeFound = AppealType.withName(appealType)
    getCaseAndRespond(
      reference,
      `case` => statusForm.bindFromRequest().fold(
        formWithErrors => successful(Ok(views.html.appeal_choose_status(`case`, appealTypeFound, formWithErrors))),
        appealStatus => caseService.addAppeal(`case`, appealTypeFound, appealStatus, request.operator).map( _ => Redirect(routes.AppealCaseController.appealDetails(reference)))
      )
    )
  }

  def confirmChangeStatus(reference: String, appealId: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.APPEAL_CASE)).async { implicit request =>
    getCaseAndRespond(
      reference,
      `case` => {
        `case`.findAppeal(appealId) match {
          case Some(appeal) => statusForm.bindFromRequest().fold(
            formWithErrors => successful(Ok(views.html.appeal_change_status(`case`, appeal, formWithErrors))),
            appealStatus => caseService.updateAppealStatus(`case`, appeal, appealStatus, request.operator).map( _ => Redirect(routes.AppealCaseController.appealDetails(reference)))
          )
          case None => successful(Ok(views.html.case_details(`case`, CaseDetailPage.APPEAL, views.html.partials.appeal.appeal_details(`case`))))
        }
      }
    )
  }

}
