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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{DecisionForm, LiabilityDetailsForm}
import uk.gov.hmrc.tariffclassificationfrontend.models.TabIndexes.tabIndexFor
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedCaseRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Decision, _}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage.{CaseDetailPage, LIABILITY}
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.liabilities.liability_details_edit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class LiabilityController @Inject()(verify: RequestActions,
                                    decisionForm: DecisionForm,
                                    val messagesApi: MessagesApi,
                                    casesService: CasesService,
                                    implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private lazy val menuTitle = LIABILITY

  def liabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    getCaseAndRenderView(menuTitle,
      c => {
        val df: Form[Decision] = decisionForm.liabilityCompleteForm(c.decision.getOrElse(Decision()))
        val lf: Form[LiabilityOrder] = LiabilityDetailsForm.liabilityDetailsCompleteForm(c.application.asLiabilityOrder)
        successful(views.html.partials.liability_details(c, tabIndexFor(LIABILITY), lf, df))
      }
    )
  }

  def editLiabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    request.`case` match {
      case c: Case =>
        successful(
          Ok(
            liability_details_edit(c, LiabilityDetailsForm.liabilityDetailsForm(c.application.asLiabilityOrder), Some(tabIndexFor(LIABILITY)))
          )
        )
    }
  }

  def postLiabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    LiabilityDetailsForm.liabilityDetailsForm(request.`case`.application.asLiabilityOrder).bindFromRequest.fold(
      errorForm => successful(Ok(liability_details_edit(request.`case`, errorForm))),
      updatedLiability => getCaseAndRedirect(menuTitle, c =>
        for {
          update <- casesService.updateCase(c.copy(application = updatedLiability))
        } yield routes.LiabilityController.liabilityDetails(update.reference)
      )
    )
  }

  private def getCaseAndRenderView(page: CaseDetailPage, toHtml: Case => Future[HtmlFormat.Appendable])
                                  (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    toHtml(request.`case`).map(html => Ok(views.html.case_details(request.`case`, page, html, activeTab = Some("tab-item-Liability"))))
  }


  private def getCaseAndRedirect(page: CaseDetailPage, toResult: Case => Future[Call])
                                (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    toResult(request.`case`).map(Redirect)
  }

}

