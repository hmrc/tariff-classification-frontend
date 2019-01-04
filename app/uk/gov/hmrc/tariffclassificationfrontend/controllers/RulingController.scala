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
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{DecisionForm, DecisionFormData, DecisionFormMapper}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class RulingController @Inject()(authenticatedAction: AuthenticatedAction,
                                 casesService: CasesService,
                                 mapper: DecisionFormMapper,
                                 val messagesApi: MessagesApi,
                                 implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private lazy val decisionForm: Form[DecisionFormData] = DecisionForm.form

  private lazy val menuTitle = CaseDetailPage.RULING

  def editRulingDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, menuTitle, c => {
      val formData = mapper.caseToDecisionFormData(c)
      val df = decisionForm.fill(formData)
      successful(views.html.partials.ruling_details_edit(c, df))
    })
  }

  def updateRulingDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    decisionForm.bindFromRequest.fold(
      errorForm =>
        getCaseAndRenderView(reference, menuTitle, c => successful(views.html.partials.ruling_details_edit(c, errorForm))),

      validForm =>
        getCaseAndRenderView(reference, menuTitle, c => {
          casesService.updateCase(mapper.mergeFormIntoCase(c, validForm)).map(views.html.partials.ruling_details(_))
        })
    )
  }

  private def getCaseAndRenderView(reference: String, page: CaseDetailPage, toHtml: Case => Future[HtmlFormat.Appendable])
                                  (implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).flatMap {
      case Some(c: Case) if c.status == CaseStatus.OPEN => toHtml(c).map(html => Ok(views.html.case_details(c, page, html)))
      case Some(_) => successful(Redirect(routes.CaseController.rulingDetails(reference)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

}
