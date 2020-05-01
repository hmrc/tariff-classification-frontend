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
import models.forms.{DecisionForm, LiabilityDetailsForm}
import javax.inject.{Inject, Singleton}
import models.request.AuthenticatedCaseRequest
import models.{Case, Decision, _}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.HtmlFormat
import service.CasesService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.CaseDetailPage.{CaseDetailPage, LIABILITY}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class LiabilityController @Inject()(
  verify: RequestActions,
  decisionForm: DecisionForm,
  mcc: MessagesControllerComponents,
  casesService: CasesService,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc) with I18nSupport {

  private lazy val menuTitle = LIABILITY

  def liabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    getCaseAndRenderView(menuTitle,
      c => {
        val df: Form[Decision] = decisionForm.liabilityCompleteForm(c.decision.getOrElse(Decision()))
        val lf: Form[Case] = LiabilityDetailsForm.liabilityDetailsCompleteForm(c)
        successful(views.html.partials.liabilities.liability_details(c, lf, df))
      }
    )
  }

  def editLiabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.EDIT_LIABILITY)).async { implicit request =>
    successful(
      Ok(
        views.html.partials.liabilities.liability_details_edit(request.`case`, LiabilityDetailsForm.liabilityDetailsForm(request.`case`))
      )
    )
  }

  def postLiabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.EDIT_LIABILITY)).async { implicit request =>
    LiabilityDetailsForm.liabilityDetailsForm(request.`case`).discardingErrors.bindFromRequest.fold(
      errorForm => successful(Ok(views.html.partials.liabilities.liability_details_edit(request.`case`, errorForm))),
      updatedCase => getCaseAndRedirect(menuTitle, c =>
        for {
          update <- casesService.updateCase(updatedCase)
        } yield routes.LiabilityController.liabilityDetails(update.reference)
      )
    )
  }

  private def getCaseAndRenderView(page: CaseDetailPage, toHtml: Case => Future[HtmlFormat.Appendable])
                                  (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    toHtml(request.`case`).map(html => Ok(views.html.case_details(request.`case`, page, html, activeTab = Some(ActiveTab.Liability))))
  }


  private def getCaseAndRedirect(page: CaseDetailPage, toResult: Case => Future[Call])
                                (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    toResult(request.`case`).map(Redirect)
  }

}

